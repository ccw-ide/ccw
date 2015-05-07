/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     François Rey - The whole of this class is mostly a copy of
 *                    code found in
 *                    org.eclipse.swt.custom.StyledText
 *******************************************************************************/
package ccw.repl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;

/**
 * A StyledRangeCache stores StyleRanges, merging them as necessary.
 * <p/>
 * This class is mostly a duplication of styling code found in
 * {@link org.eclipse.swt.custom.StyledText}. In particular
 * it replicates the style methods disabled when setting a
 * {@link org.eclipse.swt.custom.LineStyleListener}, which is why
 * it can prove useful when switching to styling based on such listener.
 * This class is not a widget class: adding style won't redraw any text
 * and it's perfectly fine to add styles before the corresponding text
 * is added to the widget.
 */
public class StyleRangeCache {
	StyleRangeCacheStore renderer; // named as such for easier code copying

	public StyleRangeCache(Device device) {
		super();
		this.renderer = new StyleRangeCacheStore(device);
	}

	/**
	 * Returns the ranges of text that have an associated StyleRange.
	 * <p>
	 * The ranges array contains start and length pairs.  Each pair refers to
	 * the corresponding style in the styles array.  For example, the pair
	 * that starts at ranges[n] with length ranges[n+1] uses the style
	 * at styles[n/2] returned by <code>getStyleRanges(int, int, boolean)</code>.
	 * </p>
	 *
	 * @param start the start offset of the style ranges to return
	 * @param length the number of style ranges to return
	 * 
	 * @return the ranges or an empty array.
	 * 
	 * 
	 * @see #getStyleRanges(int, int, boolean)
	 */
	public int[] getRanges(int start, int length) {
		int[] ranges = renderer.getRanges(start, length);
		if (ranges != null) return ranges;
		return new int[0];
	}

	/**
	 * Returns the style range at the given offset.
	 * <p/>
	 *
	 * @param offset the offset to return the style for. 
	 * 	0 <= offset < getCharCount() must be true.
	 * @return a StyleRange with start == offset and length == 1, indicating
	 * 	the style at the given offset, or null if a style is not set for
	 *  the given offset.
	 */
	public StyleRange getStyleRangeAtOffset(int offset) {
		StyleRange[] ranges = renderer.getStyleRanges(offset, 1, true);
		if (ranges != null) return ranges[0];
		return null;
	}

	/**
	 * Returns the styles for the given text range.
	 * <p>
	 * Note: Because the StyleRange includes the start and length, the
	 * same instance cannot occur multiple times in the array of styles.
	 * If the same style attributes, such as font and color, occur in
	 * multiple StyleRanges, <code>getStyleRanges(int, int, boolean)</code>
	 * can be used to get the styles without the ranges.
	 * </p>
	 * @param start the start offset of the style ranges to return
	 * @param length the number of style ranges to return
	 *
	 * @return the styles or an empty array.
	 *  The returned styles will reflect the given range.  The first 
	 *  returned <code>StyleRange</code> will have a starting offset >= start 
	 *  and the last returned <code>StyleRange</code> will have an ending 
	 *  offset <= start + length - 1
	 *
	 * 
	 * @see #getStyleRanges(int, int, boolean)
	 * 
	 */
	public StyleRange[] getStyleRanges(int start, int length) {
		return getStyleRanges(start, length, true);
	}

	/**
	 * Returns the styles for the given text range.
	 * <p>
	 * Note: When <code>includeRanges</code> is true, the start and length
	 * fields of each StyleRange will be valid, however the StyleRange
	 * objects may need to be cloned. When <code>includeRanges</code> is
	 * false, <code>getRanges(int, int)</code> can be used to get the
	 * associated ranges.
	 * </p>
	 * 
	 * @param start the start offset of the style ranges to return
	 * @param length the number of style ranges to return
	 * @param includeRanges whether the start and length field of the StyleRanges should be set.
	 *
	 * @return the styles or an empty array.
	 *  The returned styles will reflect the given range.  The first 
	 *  returned <code>StyleRange</code> will have a starting offset >= start 
	 *  and the last returned <code>StyleRange</code> will have an ending 
	 *  offset <= start + length - 1
	 * 
	 * @see #getRanges(int, int)
	 * @see #setStyleRanges(int[], StyleRange[])
	 */
	public StyleRange[] getStyleRanges(int start, int length, boolean includeRanges) {
		StyleRange[] ranges = renderer.getStyleRanges(start, length, includeRanges);
		if (ranges != null) return ranges;
		return new StyleRange[0];
	}

	/** 
	 * Replaces the styles in the given range with new styles.  This method
	 * effectively deletes the styles in the given range and then adds the
	 * the new styles. 
	 * <p>
	 * Note: Because a StyleRange includes the start and length, the
	 * same instance cannot occur multiple times in the array of styles.
	 * If the same style attributes, such as font and color, occur in
	 * multiple StyleRanges, <code>setStyleRanges(int, int, int[], StyleRange[])</code>
	 * can be used to share styles and reduce memory usage.
	 * </p>
	 *
	 * @param start offset of first character where styles will be deleted
	 * @param length length of the range to delete styles in
	 * @param ranges StyleRange objects containing the new style information.
	 * The ranges should not overlap and should be within the specified start 
	 * and length. The style rendering is undefined if the ranges do overlap
	 * or are ill-defined. Must not be null.
	 * @exception IllegalArgumentException <ul>
	 *   <li>ERROR_INVALID_RANGE when either start or end are invalid</li> 
	 *   <li>ERROR_NULL_ARGUMENT when ranges is null</li>
	 * </ul>
	 * 
	 * @see #setStyleRanges(int, int, int[], StyleRange[])
	 */
	public void replaceStyleRanges(int start, int length, StyleRange[] ranges) {
	 	if (ranges == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	 	setStyleRanges(start, length, null, ranges, false);
	}

	void setFont(Font font) {
		renderer.setFont(font);
	}

	/** 
	 * Adds the specified style.
	 * <p>
	 * The new style overwrites existing styles for the specified range.
	 * Existing style ranges are adjusted if they partially overlap with 
	 * the new style. To clear an individual style, call setStyleRange 
	 * with a StyleRange that has null attributes. 
	 * </p>
	 *
	 * @param range StyleRange object containing the style information.
	 * Overwrites the old style in the given range. May be null to delete
	 * all styles.
	 * @exception IllegalArgumentException <ul>
	 *   <li>ERROR_INVALID_RANGE when the style range is negative</li> 
	 * </ul>
	 */
	public void setStyleRange(StyleRange range) {
		if (range != null) {
			if (range.isUnstyled()) {
				setStyleRanges(range.start, range.length, null, null, false);
			} else {
				setStyleRanges(range.start, 0, null, new StyleRange[]{range}, false);
			}
		} else {
			setStyleRanges(0, 0, null, null, true);
		}
	}

	/** 
	 * Clears the styles in the range specified by <code>start</code> and 
	 * <code>length</code> and adds the new styles.
	 * <p>
	 * The ranges array contains start and length pairs.  Each pair refers to
	 * the corresponding style in the styles array.  For example, the pair
	 * that starts at ranges[n] with length ranges[n+1] uses the style
	 * at styles[n/2].  The range fields within each StyleRange are ignored.
	 * If ranges or styles is null, the specified range is cleared.
	 * </p><p>
	 * Note: It is expected that the same instance of a StyleRange will occur
	 * multiple times within the styles array, reducing memory usage.
	 * </p>
	 *
	 * @param start offset of first character where styles will be deleted
	 * @param length length of the range to delete styles in
	 * @param ranges the array of ranges.  The ranges must not overlap and must be in order.
	 * @param styles the array of StyleRanges.  The range fields within the StyleRange are unused.
	 * 
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT when an element in the styles array is null</li>
	 *    <li>ERROR_INVALID_RANGE when the number of ranges and style do not match (ranges.length * 2 == styles.length)</li> 
	 *    <li>ERROR_INVALID_RANGE when a range is negative</li> 
	 *    <li>ERROR_INVALID_RANGE when a range overlaps</li> 
	 * </ul>
	 * 
	 */
	public void setStyleRanges(int start, int length, int[] ranges, StyleRange[] styles) {
		if (ranges == null || styles == null) {
			setStyleRanges(start, length, null, null, false);
		} else {
			setStyleRanges(start, length, ranges, styles, false);
		}
	}

	/** 
	 * Sets styles to be used for rendering the widget content.
	 * <p>
	 * All styles in the widget will be replaced with the given set of ranges and styles.
	 * The ranges array contains start and length pairs.  Each pair refers to
	 * the corresponding style in the styles array.  For example, the pair
	 * that starts at ranges[n] with length ranges[n+1] uses the style
	 * at styles[n/2].  The range fields within each StyleRange are ignored.
	 * If either argument is null, the styles are cleared.
	 * </p><p>
	 * Note: It is expected that the same instance of a StyleRange will occur
	 * multiple times within the styles array, reducing memory usage.
	 * </p>
	 *
	 * @param ranges the array of ranges.  The ranges must not overlap and must be in order.
	 * @param styles the array of StyleRanges.  The range fields within the StyleRange are unused.
	 * 
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT when an element in the styles array is null</li>
	 *    <li>ERROR_INVALID_RANGE when the number of ranges and style do not match (ranges.length * 2 == styles.length)</li> 
	 *    <li>ERROR_INVALID_RANGE when a range is negative</li> 
	 *    <li>ERROR_INVALID_RANGE when a range overlaps</li> 
	 * </ul>
	 * 
	 * @since 3.2 
	 */
	public void setStyleRanges(int[] ranges, StyleRange[] styles) {
		if (ranges == null || styles == null) {
			setStyleRanges(0, 0, null, null, true);
		} else {
			setStyleRanges(0, 0, ranges, styles, true);
		}
	}

	void setStyleRanges(int start, int length, int[] ranges, StyleRange[] styles, boolean reset) {
		int end = start + length;
		if (start > end || start < 0) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}
		if (styles != null) {
			if (ranges != null) {
				if (ranges.length != styles.length << 1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
			int lastOffset = 0;
			for (int i = 0; i < styles.length; i ++) {
				if (styles[i] == null) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
				int rangeStart, rangeLength;
				if (ranges != null) {
					rangeStart = ranges[i << 1];
					rangeLength = ranges[(i << 1) + 1];
				} else {
					rangeStart = styles[i].start;
					rangeLength = styles[i].length;
				}
				if (rangeLength < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT); 
				if (lastOffset > rangeStart) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
				lastOffset = rangeStart + rangeLength;
			}
		}
		if (reset) {
			renderer.setStyleRanges(null, null);
		} else {
			renderer.updateRanges(start, length, length);
		}
		if (styles != null && styles.length > 0) {
			renderer.setStyleRanges(ranges, styles);
		}
	}

	/** 
	 * Sets styles to be used for rendering the widget content. All styles 
	 * in the widget will be replaced with the given set of styles.
	 * <p>
	 * Note: Because a StyleRange includes the start and length, the
	 * same instance cannot occur multiple times in the array of styles.
	 * If the same style attributes, such as font and color, occur in
	 * multiple StyleRanges, <code>setStyleRanges(int[], StyleRange[])</code>
	 * can be used to share styles and reduce memory usage.
	 * </p>
	 *
	 * @param ranges StyleRange objects containing the style information.
	 * The ranges should not overlap. The style rendering is undefined if 
	 * the ranges do overlap. Must not be null. The styles need to be in order.
	 * 
	 * @see #setStyleRanges(int[], StyleRange[])
	 */
	public void setStyleRanges(StyleRange[] ranges) {
	 	if (ranges == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		setStyleRanges(0, 0, null, ranges, true);
	}
	
	/** 
	 * Reset all styles (deletes all styles and ranges). 
	 */
	public void reset() {
		renderer.reset();
	}
}
