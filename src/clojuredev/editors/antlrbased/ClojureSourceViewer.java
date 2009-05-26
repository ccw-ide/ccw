/*******************************************************************************
 * Copyright (c) 2009 Stephan Muehlstrasser.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Stephan Muehlstrasser - initial implementation
 *******************************************************************************/

package clojuredev.editors.antlrbased;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import clojuredev.ClojuredevPlugin;
import clojuredev.lexers.ClojureLexer;
import clojuredev.utils.editors.antlrbased.IScanContext;

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
        initializeViewerColors();
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
            initializeViewerColors();
        }
    }
    
    protected void initializeViewerColors() {
        if (fPreferenceStore != null) {
            registerEditorColors();
        }
    }

    public void configure(SourceViewerConfiguration configuration) {
        super.configure(configuration);

        if (fPreferenceStore != null) {
            fPreferenceStore.addPropertyChangeListener(this);
            initializeViewerColors();
        }

        fIsConfigured= true;
    }
    
    /*
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     * @since 3.0
     */
    public void unconfigure() {
        if (fPreferenceStore != null)
            fPreferenceStore.removePropertyChangeListener(this);

        super.unconfigure();

        fIsConfigured= false;
    }
    
    public void registerEditorColors() {
        ColorRegistry colorRegistry = ClojuredevPlugin.getDefault().getColorRegistry();
        
        // TODO: define separate preferences for the tokens that use black and gray?
        RGB black = new RGB(0,0,0);
        RGB gray = new RGB(128,128,128);
            
        RGB literal = PreferenceConverter.getColor(fPreferenceStore, clojuredev.preferences.PreferenceConstants.EDITOR_LITERAL_COLOR);
        RGB specialForm = PreferenceConverter.getColor(fPreferenceStore, clojuredev.preferences.PreferenceConstants.EDITOR_SPECIAL_FORM_COLOR);
        RGB function = PreferenceConverter.getColor(fPreferenceStore, clojuredev.preferences.PreferenceConstants.EDITOR_FUNCTION_COLOR);
        RGB comment = PreferenceConverter.getColor(fPreferenceStore, clojuredev.preferences.PreferenceConstants.EDITOR_COMMENT_COLOR);
        RGB globalVar = PreferenceConverter.getColor(fPreferenceStore, clojuredev.preferences.PreferenceConstants.EDITOR_GLOBAL_VAR_COLOR);
        RGB keyword = PreferenceConverter.getColor(fPreferenceStore, clojuredev.preferences.PreferenceConstants.EDITOR_KEYWORD_COLOR);
        RGB metadataTypehint = PreferenceConverter.getColor(fPreferenceStore, clojuredev.preferences.PreferenceConstants.EDITOR_METADATA_TYPEHINT_COLOR);
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.STRING, literal); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NUMBER, literal); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CHARACTER, literal); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.NIL, literal); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.BOOLEAN, literal); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.OPEN_PAREN, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.CLOSE_PAREN, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SPECIAL_FORM, specialForm); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SYMBOL, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.FUNCTION, function); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.GLOBAL_VAR, globalVar); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.MACRO, specialForm); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.SPECIAL_FORM, specialForm); //$NON-NLS-1$
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_CLASS, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_STATIC_METHOD, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + IScanContext.SymbolType.JAVA_INSTANCE_METHOD, black); //$NON-NLS-1$
        
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.KEYWORD, keyword); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SYNTAX_QUOTE, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.UNQUOTE_SPLICING, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.UNQUOTE, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.COMMENT, comment); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.SPACE, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.LAMBDA_ARG, black); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.METADATA_TYPEHINT, metadataTypehint); //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T24, black);//'&'=20 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T25, gray);//'['=23 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T26, gray);//']'=24 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T27, gray);//'{'=25 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T28, gray);//'}'=26 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T29, black);//'\''=27 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T30, black);//'^'=28 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T31, black);//'@'=29 //$NON-NLS-1$
        colorRegistry.put(AntlrBasedClojureEditor.ID + "_" + ClojureLexer.T32, black);//'#'=30 //$NON-NLS-1$
    }

}
