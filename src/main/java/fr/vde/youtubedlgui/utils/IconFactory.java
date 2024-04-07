package fr.vde.youtubedlgui.utils;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.net.URL;

public class IconFactory {
    public static ImageIcon getIconResource(String iconName) {

        String name = "/fr/vde/youtubedlgui/resources/" + iconName;
        URL url = IconFactory.class.getResource(name);
        assert(url != null);
        return new ImageIcon(url);
    }
}
