/*******************************************************************************
 * Copyright (c) 2015 Laurent Petit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Andrea RICHIARDI - initial implementation
 *******************************************************************************/
package ccw.editors.clojure.hovers;

import static org.junit.Assert.assertNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ccw.CCWPlugin;
import ccw.TraceOptions;
import ccw.util.SWTFontUtils;

/**
 * From pull request <a href="https://github.com/laurentpetit/ccw/pull/763">#763</a>:<br/>
 * All the <code>pre</code> tags were cut in the hovers.<br/><br/>
 * I then investigated and noticed that the BrowserInformationControl calculates its width in a way that
 * does not consider that <code>pre</code> tags are monospace, therefore bigger.<br/>
 *
 * Now, two options:<br/>
 * 1) Change the <code>pre</code>  tag in something different, losing monospace (that I like).<br/>
 * 2) Find a way to consider the bigger font size of <code>pre</code>.<br/><br/>
 * 
 * This class implements number option 2).
 * @author Andrea Richiardi
 *
 */
@SuppressWarnings("restriction")
public class CCWBrowserInformationControl extends BrowserInformationControl {

    protected final String fSymbolicFontName;
    
    private TextLayout fTestWidget;
    
    public CCWBrowserInformationControl(Shell parent, String symbolicFontName, boolean resizable) {
        super(parent, symbolicFontName, resizable);
        fSymbolicFontName = symbolicFontName;
    }
    
    public CCWBrowserInformationControl(Shell parent, String symbolicFontName, String statusFieldText) {
        super(parent, symbolicFontName, statusFieldText);
        fSymbolicFontName = symbolicFontName;
    }

    public CCWBrowserInformationControl(Shell parent, String symbolicFontName, ToolBarManager toolBarManager) {
        super(parent, symbolicFontName, toolBarManager);
        fSymbolicFontName = symbolicFontName;
    }

    private void createTestWidget() {
        fTestWidget= new TextLayout(getShell().getDisplay());

        // Initialize fonts
        Font font= JFaceResources.getFont(fSymbolicFontName == null ? JFaceResources.DIALOG_FONT : fSymbolicFontName);
        fTestWidget.setFont(font);
        fTestWidget.setWidth(-1);

        // Compute and set tab width
        fTestWidget.setText("    "); //$NON-NLS-1$
        int tabWidth = fTestWidget.getBounds().width;
        fTestWidget.setTabs(new int[] { tabWidth });
        fTestWidget.setText(""); //$NON-NLS-1$
    }
    
    @Override
    protected void createContent(Composite parent) {
        super.createContent(parent);
        createTestWidget();
    }

    @Override
    protected void handleDispose() {
        super.handleDispose();
        if (fTestWidget != null) {
            fTestWidget.dispose();
            fTestWidget= null;
        }
    }

    private Point estimateSizeHint(Font font, String text) {
        fTestWidget.setFont(font);
        fTestWidget.setText(text);
        
        Rectangle bounds= fTestWidget.getBounds(); // does not return minimum width, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=217446
        int height = bounds.height;
                
        int lineCount= fTestWidget.getLineCount();
        int width= 0;
        for (int i= 0; i < lineCount; i++) {
            Rectangle rect= fTestWidget.getLineBounds(i);
            width = Math.max(width, rect.x + rect.width);
        }
        fTestWidget.setText("");

        return new Point(bounds.x + width + 16, bounds.y + height + 16);
    }
    
    @Override
    public Point computeSizeHint() {
        // AR - this hack is necessary because BrowserInformationControl does not take into consideration
        // <pre> tags during its computation of the displayed widget
        Point newSizeHint = super.computeSizeHint();
        
        BrowserInformationControlInput input = getInput();
        if (input != null) {
            Document doc = Jsoup.parse(input.getHtml());
            Element styleElement = doc.select("style").first();
            
            int preCssPoints = -1;
            Matcher preCssPointsMatcher = Pattern.compile("pre.*[{].*font-size:[ ]+(\\d+)p").matcher(styleElement.html());
            if (preCssPointsMatcher.find()) {
                try {
                    preCssPoints = Integer.valueOf(preCssPointsMatcher.group(1));
                    CCWPlugin.getTracer().trace(TraceOptions.HOVER_SUPPORT, "CSS <pre> tag points found: " + preCssPoints);
                } catch (NumberFormatException ex) {
                    CCWPlugin.getTracer().trace(TraceOptions.HOVER_SUPPORT, "CSS <pre> NumberFormatException, cannot read from it.");
                }
            }
            
            // AR - Given that we are in *hack-mode*, for the height we will first subtract
            // the estimate calculated with the used font and then sum the one obtained using
            // the Monospace font.
            Font originalFont = JFaceResources.getFont(fSymbolicFontName);

            FontData[] monoFd = SWTFontUtils.getMonospacedFont().getFontData();
            assertNotNull(monoFd);
            
            Font monospaceFont;
            if (preCssPoints != -1) {
                monospaceFont = new Font(getShell().getDisplay(), SWTFontUtils.newHeightFontData(monoFd, preCssPoints));
            } else {
                monospaceFont = new Font(getShell().getDisplay(), monoFd);
            }
            
            Elements preElements = doc.getElementsByTag("pre");
            for (Element el : preElements) {
                String txt = el.text();

                // AR - Estimate with original font
                Point originalSize = estimateSizeHint(originalFont, txt);

                // AR - Jsoup flattens all the tag's texts. 
                Point monospaceSize = estimateSizeHint(monospaceFont, txt);

                // AR - Canonical "+ something" as in BrowserInformationControl.computeSizeHint
                newSizeHint = new Point(Math.max(newSizeHint.x, monospaceSize.x),
                        Math.max(newSizeHint.y, newSizeHint.y - originalSize.y + monospaceSize.y));
            }
            monospaceFont.dispose();
            
            // AR - Considering <p> as occuping more than nothing in height 
            Elements pElements = doc.getElementsByTag("p");
            
            // AR - Trim for taking into consideration the scrollbars
            Rectangle trim = computeTrim();
            
            // AR - final sum
            newSizeHint = new Point(newSizeHint.x + trim.width, newSizeHint.y + trim.height + pElements.size() * 6);
        }
        return newSizeHint;
    }
}
