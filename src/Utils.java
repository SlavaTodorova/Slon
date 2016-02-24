import java.io.File;

/**
 * From https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html#filters 
 * Modified by Slava Todorova
 *
 */
public class Utils {

    public final static String txt = "txt";
    public final static String slon = "slon";

    /*
     * Get the extension of a file.
     */  
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}