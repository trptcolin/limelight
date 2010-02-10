package limelight.ui.model;

import java.awt.*;

public class LineSelectionProcessor extends LineProcessor
{
  public LineSelectionProcessor(TextRenderer textRenderer, Graphics2D graphics, int y, boolean firstEndpointDrawn, boolean lastEndpointDrawn, StringBuffer highlightedText, int lineIndex, int charactersDrawn)
  {
    super(textRenderer, graphics, y, firstEndpointDrawn, lastEndpointDrawn, highlightedText, lineIndex, charactersDrawn);
  }

  @Override
  protected boolean isSelected()
  {
    return textRenderer.startSelectionPoint != null &&
            textRenderer.endSelectionPoint != null &&
            textRenderer.getEndSelectionIndex() > 0 &&
            ((firstEndpointDrawn && !lastEndpointDrawn) ||
              containsStartOrEnd());
  }

  private boolean isWithinLine(int index)
  {

    int startOfLine = charactersDrawn;
    int endOfLine = charactersDrawn + textLayout.getCharacterCount();

    return startOfLine <= index && endOfLine > index;
  }

  @Override
  public boolean containsStartAndEnd()
  {
    return (isWithinLine(textRenderer.getStartSelectionIndex()) && isWithinLine(textRenderer.getEndSelectionIndex()));
  }

  @Override
  protected boolean containsStartOrEnd()
  {
    return (isWithinLine(textRenderer.getStartSelectionIndex()) || isWithinLine(textRenderer.getEndSelectionIndex()));
  }

  @Override
  protected Shape processMiddleOfLine()
  {
    // all of selection included (select middle)

    int startIndex = textRenderer.getStartSelectionIndex() - charactersDrawn;
    int endIndex = textRenderer.getEndSelectionIndex() - charactersDrawn;

    if (startIndex > endIndex)
    {
      int temp = startIndex;
      startIndex = endIndex;
      endIndex = temp;
    }
    Shape highlight = textLayout.getLogicalHighlightShape(startIndex, endIndex);
    highlightedText.append(textRenderer.getActualLineTexts().get(lineIndex).substring(startIndex, endIndex));
    firstEndpointDrawn = true;
    lastEndpointDrawn = true;
    return highlight;
  }

  @Override
  protected Shape processEndOfLine()
  {
    Shape highlight;

    int index;
    if (isWithinLine(textRenderer.getStartSelectionIndex()))
      index = textRenderer.getStartSelectionIndex() - charactersDrawn;
    else
      index = textRenderer.getEndSelectionIndex() - charactersDrawn;

    if (firstEndpointDrawn)
    {
      // end of selection included (select from start of line)
      highlight = textLayout.getLogicalHighlightShape(0, index);
      highlightedText.append(textRenderer.getActualLineTexts().get(lineIndex).substring(0, index));
      lastEndpointDrawn = true;
    }
    else
    {
      // start of selection included (select from end of line)
      highlight = textLayout.getLogicalHighlightShape(index, textLayout.getCharacterCount());
      highlightedText.append(textRenderer.getActualLineTexts().get(lineIndex).substring(index, textLayout.getCharacterCount()));
      firstEndpointDrawn = true;
    }
    return highlight;
  }
}
