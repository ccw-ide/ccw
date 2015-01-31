package ccw.util;

import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

/**
 * Editor utilities, largely inspired from org.eclipse.jdt.
 * @author Andrea Richiardi
 */
public class EditorUtility {

    /**
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
     * Returns the modifier string for the given SWT modifier.
     * modifier bits.
     *
     * @param stateMask	The SWT modifier bits
     * @return The modifier string or null if the input is SWT.DEFAULT or empty string otherwise (it follows computeStateMask's behaviour).
     */
    public static String getModifierString(int stateMask) {
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
     * Appends to modifier string of the given SWT modifier bit
     * to the given modifierString.
     *
     * @param modifierString	the modifier string
     * @param modifier			an int with SWT modifier bit
     * @return the concatenated modifier string
     * @since 2.1.1
     */
    private static String appendModifierString(@Nullable String modifierString, int modifier) {
        if (modifierString == null) {
            modifierString= ""; //$NON-NLS-1$
        }
        String newModifierString= Action.findModifierString(modifier);
        if (modifierString.length() == 0) {
            return newModifierString;
        }
        return String.format("%s + %s", modifierString, newModifierString);
    }
}
