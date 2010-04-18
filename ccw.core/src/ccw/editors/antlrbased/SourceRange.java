package ccw.editors.antlrbased;

import org.eclipse.jdt.core.ISourceRange;

public class SourceRange implements ISourceRange {
	private final int offset;
	private final int length;
	
	public SourceRange(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}
	
	public int getLength() {
		return length;
	}
}
