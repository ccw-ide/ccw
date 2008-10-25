/*
 * ScalaColorProvider.java
 * 
 * Created on 04.11.2004
 *
 * Status: done
 */
package clojuredev.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import clojuredev.ClojuredevPlugin;

/**
 * @author Marc Moser
 * 
 * This provides the colors for syntax highlighting.
 */
public class ClojureColorProvider implements ClojureColorConstants {

	protected Map<String,Color> colorMap = new HashMap<String,Color>();
	public final Map<String,Font> fontMap = new HashMap<String,Font>();

	public ClojureColorProvider() {
		super();
	}

	public Color getColor(String colorKey) {
		Color color = (Color) colorMap.get(colorKey);
		if (color == null) {
			RGB rgb = PreferenceConverter.getColor(ClojuredevPlugin.getDefault().getPreferenceStore(), colorKey);
			color = new Color(Display.getCurrent(), rgb);
			colorMap.put(colorKey, color);
		}

		return color;
	}

	public void removeColor(String colorKey) {
		colorMap.remove(colorKey);
		fontMap.remove(colorKey);
	}
	
	public Font getFont(String colorKey, Font font) {
		if (colorKey == null) return null;
		Font ret = fontMap.get(colorKey);
		if (ret != null) return ret;
		if (font == null) return font;
		
		IPreferenceStore prefs = ClojuredevPlugin.getDefault().getPreferenceStore();
		
		boolean bold = prefs.getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISBOLD_APPENDIX);
		boolean italics = prefs.getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISITALICS_APPENDIX);
		boolean underline = prefs.getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISUNDERLINE_APPENDIX);

		boolean strikethrough = prefs.getBoolean(
				colorKey + ClojureColorConstants.CLOJURE_ISSTRIKETHROUGH_APPENDIX);
		
		int height = font.getFontData()[0].getHeight();
		String name = font.getFontData()[0].getName();
		int style = SWT.NORMAL;
		
		if (bold) style = style | SWT.BOLD;
		if (underline) style = style | TextAttribute.UNDERLINE;
		if (italics) style = style | SWT.ITALIC;
		if (strikethrough) style = style | TextAttribute.STRIKETHROUGH;
		
		FontData fd = new FontData(name, height, style);
		ret = new Font(font.getDevice(), fd);
		fontMap.put(colorKey, ret);
		return ret;
	}
	
	private Map<ImageDescriptor,Image> images = new HashMap<ImageDescriptor,Image>();
	public Image image(ImageDescriptor desc) {
		if (!images.containsKey(desc)) images.put(desc, desc.createImage());
		return images.get(desc);
	}
  
//  public Image getImage(String prefix, String name) {
//    ImageDescriptor desc = ScalaPluginImages.image(prefix, name);
//    return image(desc);
//  }
//
//	public Image getImage(Object element) { 
//		if (element instanceof Models.ClassMod) {
//			Models.ClassMod clazz = (Models.ClassMod) element;
//			if (clazz.tree().symbol().isTrait())
//				return image(ScalaPluginImages.getObj("trait", ""));
//			else return image(ScalaPluginImages.getObj("class", ""));
//		}
//		else if (element instanceof Models.ObjectMod) return image(ScalaPluginImages.getObj("object", ""));
//		else if (element instanceof Models.ValMod || element instanceof Models.DefMod) {
//			Trees.ValOrDefDef tree = (Trees.ValOrDefDef) ((Models.ValOrDefMod) element).tree();
//			String kind = element instanceof Models.ValMod ? "val" : "def";
//			String access;
//			if (tree.mods().isPrivate()) access = "pri";
//			else if (tree.mods().isProtected()) access = "pro";
//			else access = "pub";
//			return image(ScalaPluginImages.getObj(kind, access));
//		}
//		/*
//		else if (element instanceof Models.AbsTypeMod) {
//			return image(ScalaPluginImages.getObj("typevariable", ""));
//		}
//		else if (element instanceof Models.AbsTypeMod || element instanceof Models.AliasTypeMod) {
//			return image(ScalaPluginImages.getObj("typevariable", ""));
//		}*/
//		
//		
//		else return null;
//	}
//
//	public Image getImage0(Object orig0, Object actual0) {
//		Symbols.Symbol orig = (Symbols.Symbol) orig0; 
//		Symbols.Symbol actual = (Symbols.Symbol) actual0;
//		String access = "";
//		
//		String kind;
//		if (actual.isValue() || actual.isMethod()) {
//			if (actual.isValue()) kind = "val";
//			else kind = "def";
//			if (orig.hasFlag(Flags.PROTECTED())) access = "pro";
//			else access = "pub";
//		}
//		else if (actual.isMethod()) kind = "def";
//		else if (actual.isModule()) kind = "object";
//		else if (actual.isClass())  kind = "class";
//		else if (actual.isAbstractType() || actual.isAliasType()) kind = "typevariable";
//		else return null;
//		return image(ScalaPluginImages.getObj(kind, access));
//	}
}
