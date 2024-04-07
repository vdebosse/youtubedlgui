package fr.vde.youtubedlgui.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleExtractor {
   private static final Pattern TITLE_TAG = Pattern.compile("\\<title>(.*)\\</title>", 34);

   public static String getPageTitle(String url) throws IOException {
      URL u = new URL(url);
      URLConnection conn = u.openConnection();
      TitleExtractor.ContentType contentType = getContentTypeHeader(conn);
      if (!contentType.contentType.equals("text/html")) {
         return null;
      } else {
         Charset charset = getCharset(contentType);
         if (charset == null) {
            charset = Charset.defaultCharset();
         }

         InputStream in = conn.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
         int n = 0;
         int totalRead = 0;
         char[] buf = new char[1024];

         StringBuilder content;
         for(content = new StringBuilder(); totalRead < 8192 && (n = reader.read(buf, 0, buf.length)) != -1; totalRead += n) {
            content.append(buf, 0, n);
         }

         reader.close();
         Matcher matcher = TITLE_TAG.matcher(content);
         return matcher.find() ? matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim() : null;
      }
   }

   private static TitleExtractor.ContentType getContentTypeHeader(URLConnection conn) {
      int i = 0;
      boolean moreHeaders = true;

      do {
         String headerName = conn.getHeaderFieldKey(i);
         String headerValue = conn.getHeaderField(i);
         if (headerName != null && headerName.equals("Content-Type")) {
            return new TitleExtractor.ContentType(headerValue, (TitleExtractor.ContentType)null);
         }

         ++i;
         moreHeaders = headerName != null || headerValue != null;
      } while(moreHeaders);

      return null;
   }

   private static Charset getCharset(TitleExtractor.ContentType contentType) {
      return contentType != null && contentType.charsetName != null && Charset.isSupported(contentType.charsetName) ? Charset.forName(contentType.charsetName) : null;
   }

   private static final class ContentType {
      private static final Pattern CHARSET_HEADER = Pattern.compile("charset=([-_a-zA-Z0-9]+)", 34);
      private String contentType;
      private String charsetName;

      private ContentType(String headerValue) {
         if (headerValue == null) {
            throw new IllegalArgumentException("ContentType must be constructed with a not-null headerValue");
         } else {
            int n = headerValue.indexOf(";");
            if (n != -1) {
               this.contentType = headerValue.substring(0, n);
               Matcher matcher = CHARSET_HEADER.matcher(headerValue);
               if (matcher.find()) {
                  this.charsetName = matcher.group(1);
               }
            } else {
               this.contentType = headerValue;
            }

         }
      }

      // $FF: synthetic method
      ContentType(String var1, TitleExtractor.ContentType var2) {
         this(var1);
      }
   }
}
