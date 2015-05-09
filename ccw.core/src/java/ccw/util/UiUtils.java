package ccw.util;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import ccw.CCWPlugin;

/**
 * Editor utilities, largely inspired from org.eclipse.jdt, a little bit adapted.
 * @author Andrea Richiardi
 */
public class UiUtils {

    /**
     * <i>[from org.eclipse.jdt]</i><br/>
     * Computes the state mask for the given modifier string (no trim is executed on the input string).
     *
     * @param modifiers	the string with the modifiers, separated by '+', '-', ';', ',' or '.'
     * @return the state mask or SWT.DEFAULT if the input is null or invalid or SWT.NONE if the input string is empty.
     */
    public static int computeStateMask(@Nullable String modifiers) {
        if (modifiers == null) {
            return SWT.DEFAULT;
        }

        if (modifiers.length() == 0) {
            return SWT.NONE;
        }

        int stateMask= 0;
        StringTokenizer modifierTokenizer= new StringTokenizer(modifiers, ",;.:+-* "); //$NON-NLS-1$
        while (modifierTokenizer.hasMoreTokens()) {
            int modifier= findLocalizedModifier(modifierTokenizer.nextToken());
            if (modifier == 0 || (stateMask & modifier) == modifier) {
                return SWT.DEFAULT;
            }
            stateMask= stateMask | modifier;
        }
        return stateMask;
    }

    /**
     * <i>[from org.eclipse.jdt]</i><br/>
     * Maps the localized modifier name to a code in the same
     * manner as #findModifier.
     *
     * @param modifierName The modifier name.
     * @return The SWT modifier bit, or <code>0</code> if no match was found
     */
    public static int findLocalizedModifier(@Nullable String modifierName) {
        if (modifierName == null) {
            return 0;
        }
        if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.CTRL))) {
            return SWT.CTRL;
        }
        if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT))) {
            return SWT.SHIFT;
        }
        if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.ALT))) {
            return SWT.ALT;
        }
        if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND))) {
            return SWT.COMMAND;
        }

        return 0;
    }

    /**
     * <i>[from org.eclipse.jdt]</i><br/>
     * Returns the modifier string for the given SWT modifier bits.
     *
     * @param stateMask	The SWT modifier bits
     * @return The modifier string or null if the input is SWT.DEFAULT or empty string otherwise (it follows computeStateMask's behaviour).
     */
    public static @Nullable String getModifierString(int stateMask) {
        if (stateMask == SWT.DEFAULT) {
            return null;
        }

        String modifierString= ""; //$NON-NLS-1$

        if ((stateMask & SWT.CTRL) == SWT.CTRL) {
            modifierString= appendModifierString(modifierString, SWT.CTRL);
        }
        if ((stateMask & SWT.ALT) == SWT.ALT) {
            modifierString= appendModifierString(modifierString, SWT.ALT);
        }
        if ((stateMask & SWT.SHIFT) == SWT.SHIFT) {
            modifierString= appendModifierString(modifierString, SWT.SHIFT);
        }
        if ((stateMask & SWT.COMMAND) == SWT.COMMAND) {
            modifierString= appendModifierString(modifierString,  SWT.COMMAND);
        }
        return modifierString;
    }

    /**
     * <i>[from org.eclipse.jdt]</i><br/>
     * Appends to modifier string of the given SWT modifier bit
     * to the given modifierString.
     *
     * @param modifierString	the modifier string
     * @param modifier			an int with SWT modifier bit
     * @return the concatenated modifier string
     */
    private static @Nullable String appendModifierString(@Nullable String modifierString, int modifier) {
        if (modifierString == null) {
            modifierString= ""; //$NON-NLS-1$
        }
        String newModifierString= Action.findModifierString(modifier);
        if (modifierString.length() == 0) {
            return newModifierString;
        }
        return String.format("%s + %s", modifierString, newModifierString);
    }
    
    /**
     * <i>[from org.eclipse.jdt]</i><br/>
     * Returns the hover affordance string, all the ITextHover's IInformationControl/IInformationProvider should call this method in order to show their affordance string. 
     *
     * @return the affordance string which is empty if the preference is enabled
     *          but the key binding not active or <code>null</code> if the
     *          preference is disabled or the binding service is unavailable
     */
    public static final @Nullable String getTooltipAffordanceString() {
        if (!CCWPlugin.getDefault().getCombinedPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
            return null;

        IBindingService bindingService= (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
        if (bindingService == null)
            return null;

        String keySequence= bindingService.getBestActiveBindingFormattedFor(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
        if (keySequence == null)
            return ""; //$NON-NLS-1$

        return Messages.bind(Messages.HoverTooltipAffordance_message, keySequence);
    }
    
    /**
     * <i>[from org.eclipse.jdt]</i><br/>
     * Returns an RGB that lies between the given foreground and background
     * colors using the given mixing factor. A <code>factor</code> of 1.0 will produce a
     * color equal to <code>fg</code>, while a <code>factor</code> of 0.0 will produce one
     * equal to <code>bg</code>.
     * @param bg the background color
     * @param fg the foreground color
     * @param factor the mixing factor, must be in [0,&nbsp;1]
     *
     * @return the interpolated color
     */
    private static RGB blend(@NonNull RGB bg, @NonNull RGB fg, float factor) {
        // copy of org.eclipse.jface.internal.text.revisions.Colors#blend(..)
        Assert.isLegal(bg != null);
        Assert.isLegal(fg != null);
        Assert.isLegal(factor >= 0f && factor <= 1f);
        
        float complement= 1f - factor;
        return new RGB(
                (int) (complement * bg.red + factor * fg.red),
                (int) (complement * bg.green + factor * fg.green),
                (int) (complement * bg.blue + factor * fg.blue)
        );
        
    }
    
    public static Point estimateSizeHint(Device device, Font font, String text) {
        // Fake widget
        TextLayout t = new TextLayout(device);
        t.setFont(font);
        t.setText(text);
        
        Rectangle bounds = t.getBounds();
        return new Point(bounds.x + bounds.width, bounds.y + bounds.height);
    }
}
