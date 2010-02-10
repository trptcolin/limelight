package limelight.ui.model;

import limelight.util.Box;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;

public class LineProcessor
{
  protected Graphics2D graphics;
  protected int y;
  protected boolean firstEndpointDrawn;
  protected boolean lastEndpointDrawn;
  protected StringBuffer highlightedText;
  protected int lineIndex;
  protected int x;
  protected TextLayout textLayout;
  protected Rectangle rect;
  protected TextRenderer textRenderer;

  protected int charactersDrawn;

  public LineProcessor(TextRenderer textRenderer, Graphics2D graphics, int y, boolean firstEndpointDrawn, boolean lastEndpointDrawn, StringBuffer highlightedText, int lineIndex, int charactersDrawn)
  {
    this.textRenderer = textRenderer;
    this.graphics = graphics;
    this.y = y;
    this.firstEndpointDrawn = firstEndpointDrawn;
    this.lastEndpointDrawn = lastEndpointDrawn;
    this.highlightedText = highlightedText;
    this.lineIndex = lineIndex;

    this.textLayout = textRenderer.getLines().get(lineIndex);
    this.x = textRenderer.getStyle().getCompiledHorizontalAlignment().getX((int) TextRenderer.widthOf(textLayout), new Box(0, 0, textRenderer.getWidth(), textRenderer.getHeight()));
    this.rect = new Rectangle(x, (int) this.y, textRenderer.getWidth(), Math.round(textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading()));
    
    this.charactersDrawn = charactersDrawn;
  }

  public int getY()
  {
    return y;
  }

  public boolean isFirstEndpointDrawn()
  {
    return firstEndpointDrawn;
  }

  public boolean isLastEndpointDrawn()
  {
    return lastEndpointDrawn;
  }

  public LineProcessor invoke()
  {
    y += textLayout.getAscent();

    if (isSelected())
    {
      drawHighlight(graphics, y, x, highlightForLine());
    }

    textLayout.draw(graphics, x, y);
    y += textLayout.getDescent() + textLayout.getLeading();
    charactersDrawn += textLayout.getCharacterCount();
    return this;
  }

  private Shape highlightForLine()
  {
    Shape highlight;

    if(containsStartAndEnd())
    {
      highlight = processMiddleOfLine();
    }
    else if (containsStartOrEnd())
    {
      highlight = processEndOfLine();
    }
    else
    {
      highlight = processFullLine();
    }
    return highlight;
  }

  public boolean containsStartAndEnd()
  {
    return (rect.contains(textRenderer.startSelectionPoint) && rect.contains(textRenderer.endSelectionPoint));
  }

  protected boolean containsStartOrEnd()
  {
    return rect.contains(textRenderer.endSelectionPoint) || rect.contains(textRenderer.startSelectionPoint);
  }

  protected boolean isSelected()
  {
    return textRenderer.startSelectionPoint != null &&
            textRenderer.endSelectionPoint != null &&
            ((firstEndpointDrawn && !lastEndpointDrawn) ||
                    rect.contains(textRenderer.startSelectionPoint) ||
                    rect.contains(textRenderer.endSelectionPoint));
  }

  protected Shape processMiddleOfLine()
  {
    Shape highlight = null;// all of selection included (select middle)
    Point origin = new Point(x, (int) y);
    TextHitInfo startHit = computeHit(textLayout, textRenderer.startSelectionPoint, origin);
    TextHitInfo endHit = computeHit(textLayout, textRenderer.endSelectionPoint, origin);

    int startIndex = startHit.getCharIndex();
    int endIndex = endHit.getCharIndex();

    if (startIndex > endIndex)
    {
      startIndex = endHit.getCharIndex();
      endIndex = startHit.getCharIndex();
    }

    textRenderer.setStartSelectionIndex(startIndex + charactersDrawn);
    textRenderer.setEndSelectionIndex(endIndex + charactersDrawn);

    highlight = textLayout.getLogicalHighlightShape(startIndex, endIndex);
    highlightedText.append(textRenderer.getActualLineTexts().get(lineIndex).substring(startIndex, endIndex));
    firstEndpointDrawn = true;
    lastEndpointDrawn = true;
    return highlight;
  }
  
  protected Shape processFullLine()
  {
    Shape highlight = null;// full line
    highlight = textLayout.getLogicalHighlightShape(0, textLayout.getCharacterCount());
    highlightedText.append(textRenderer.getActualLineTexts().get(lineIndex).substring(0, textLayout.getCharacterCount()));
    return highlight;
  }

  protected Shape processEndOfLine()
  {
    Shape highlight = null;
    Point origin = new Point(x, (int) y);
    TextHitInfo startHit = computeHit(textLayout, textRenderer.startSelectionPoint, origin);
    TextHitInfo endHit = computeHit(textLayout, textRenderer.endSelectionPoint, origin);

    TextHitInfo hit;
    if (rect.contains(textRenderer.startSelectionPoint))
      hit = startHit;
    else
      hit = endHit;

    if (firstEndpointDrawn)
    {
      // end of selection included (select from start of line)
      textRenderer.setEndSelectionIndex(hit.getCharIndex() + charactersDrawn);
      highlight = textLayout.getLogicalHighlightShape(0, hit.getCharIndex());
      highlightedText.append(textRenderer.getActualLineTexts().get(lineIndex).substring(0, hit.getCharIndex()));
      lastEndpointDrawn = true;
    }
    else
    {
      // start of selection included (select from end of line)
      textRenderer.setStartSelectionIndex(hit.getCharIndex() + charactersDrawn);
      highlight = textLayout.getLogicalHighlightShape(hit.getCharIndex(), textLayout.getCharacterCount());
      highlightedText.append(textRenderer.getActualLineTexts().get(lineIndex).substring(hit.getCharIndex(), textLayout.getCharacterCount()));
      firstEndpointDrawn = true;
    }
    return highlight;
  }

  protected void drawHighlight(Graphics2D graphics, float y, int x, Shape highlight)
  {
    Graphics2D g = (Graphics2D) graphics.create();
    g.translate(x, y);
    g.setPaint(Color.lightGray);
    g.fill(highlight);
  }

  protected TextHitInfo computeHit(TextLayout textLayout, Point point, Point origin)
  {
    float hitX = (float) (point.getX() - origin.x);
    float hitY = (float) (point.getY() - origin.x);
    return textLayout.hitTestChar(hitX, hitY);
  }

  public int getCharactersDrawn()
  {
    return charactersDrawn;
  }
}
