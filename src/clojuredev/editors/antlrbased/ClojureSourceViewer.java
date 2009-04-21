package clojuredev.editors.antlrbased;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ClojureSourceViewer extends ProjectionViewer implements
        IPropertyChangeListener {
    /**
     * The preference store.
     */
    private IPreferenceStore fPreferenceStore;
    /**
     * Is this source viewer configured?
     */
    private boolean fIsConfigured;
    
    public ClojureSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
        setPreferenceStore(store);
    }

    public void propertyChange(PropertyChangeEvent event) {
        // TODO what needs to change here?
/*        String property = event.getProperty();
        if (AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND.equals(property)
                || AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property)
                || AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND.equals(property)
                || AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)
                || AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR.equals(property)
                || AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR.equals(property)
                || AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR.equals(property)
                || AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR.equals(property))
        {
            // initializeViewerColors();
        }*/
    }

    /**
     * Sets the preference store on this viewer.
     *
     * @param store the preference store
     *
     * @since 3.0
     */
    public void setPreferenceStore(IPreferenceStore store) {
        if (fIsConfigured && fPreferenceStore != null)
            fPreferenceStore.removePropertyChangeListener(this);

        fPreferenceStore= store;

        if (fIsConfigured && fPreferenceStore != null) {
            fPreferenceStore.addPropertyChangeListener(this);
            // initializeViewerColors();
        }
    }
}
