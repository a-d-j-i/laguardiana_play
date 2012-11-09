/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package devices.printHelper;

import javax.swing.text.Element;
import javax.swing.text.FlowView;
import javax.swing.text.ParagraphView;
import javax.swing.text.View;

/**
 *
 * @author adji
 */
class HTMLParagraphView extends ParagraphView {

    public static int MAX_VIEW_SIZE = 100;

    public HTMLParagraphView(Element elem) {
        super(elem);
        strategy = new HTMLParagraphView.HTMLFlowStrategy();
    }

    public static class HTMLFlowStrategy extends FlowView.FlowStrategy {

        protected View createView(FlowView fv, int startOffset, int spanLeft, int rowIndex) {
            View res = super.createView(fv, startOffset, spanLeft, rowIndex);
            if (res.getEndOffset() - res.getStartOffset() > MAX_VIEW_SIZE) {
                res = res.createFragment(startOffset, startOffset + MAX_VIEW_SIZE);
            }
            return res;
        }
    }

    public int getResizeWeight(int axis) {
        return 0;
    }
}
