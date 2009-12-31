package limelight.ui.model;

import limelight.util.Box;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;

public class LineProcessor
{
  private Graphics2D graphics;
  private float y;
  private boolean firstEndpointDrawn;
  private boolean lastEndpointDrawn;
  private StringBuffer highlightedText;
  private int lineIndex;
  private int x;
  private TextLayout textLayout;
  private Rectangle rect;
  private TextPanel textPanel;

  public LineProcessor(TextPanel textPanel, Graphics2D graphics, float y, boolean firstEndpointDrawn, boolean lastEndpointDrawn, StringBuffer highlightedText, int lineIndex)
  {
    this.textPanel = textPanel;
    this.graphics = graphics;
    this.y = y;
    this.firstEndpointDrawn = firstEndpointDrawn;
    this.lastEndpointDrawn = lastEndpointDrawn;
    this.highlightedText = highlightedText;
    this.lineIndex = lineIndex;

    this.textLayout = textPanel.getLines().get(lineIndex);
    this.x = textPanel.getStyle().getCompiledHorizontalAlignment().getX((int) textPanel.widthOf(textLayout), new Box(0, 0, textPanel.getWidth(), textPanel.getHeight()));
    this.rect = new Rectangle(x, (int) this.y, textPanel.getWidth(), (int) (textLayout.getBounds().getHeight()));
  }

  public float getY()
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
    return this;
  }

  public LineProcessor invokeNoDrawing()
  {
    y += textLayout.getAscent();

    if (isSelected())
    {
      drawHighlight(graphics, y, x, highlightForLine());
    }

    y += textLayout.getDescent() + textLayout.getLeading();
    return this;
  }

  private Shape highlightForLine()
  {
    Shape highlight;
    if (rect.contains(textPanel.startSelectionPoint) && rect.contains(textPanel.endSelectionPoint))
    {
      highlight = processMiddleOfLine();
    }
    else if (rect.contains(textPanel.endSelectionPoint) || rect.contains(textPanel.startSelectionPoint))
    {
      highlight = processEndOfLine();
    }
    else
    {
      highlight = processFullLine();
    }
    return highlight;
  }

  private boolean isSelected()
  {
    return textPanel.startSelectionPoint != null && textPanel.endSelectionPoint != null && ((firstEndpointDrawn && !lastEndpointDrawn) || (rect.contains(textPanel.startSelectionPoint) || rect.contains(textPanel.endSelectionPoint)));
  }

  private Shape processFullLine()
  {
    Shape highlight;// full line
    highlight = textLayout.getLogicalHighlightShape(0, textLayout.getCharacterCount());
    highlightedText.append(textPanel.actualLineTexts.get(lineIndex).substring(0, textLayout.getCharacterCount()));
    return highlight;
  }

  private Shape processEndOfLine()
  {
    Shape highlight;
    Point origin = new Point(x, (int) y);
    TextHitInfo startHit = computeHit(textLayout, textPanel.startSelectionPoint, origin);
    TextHitInfo endHit = computeHit(textLayout, textPanel.endSelectionPoint, origin);

    TextHitInfo hit;
    if (rect.contains(textPanel.startSelectionPoint))
      hit = startHit;
    else
      hit = endHit;

    if (firstEndpointDrawn)
    {
      // end of selection included (select from start of line)
      highlight = textLayout.getLogicalHighlightShape(0, hit.getCharIndex());
      highlightedText.append(textPanel.actualLineTexts.get(lineIndex).substring(0, hit.getCharIndex()));
      lastEndpointDrawn = true;
    }
    else
    {
      // start of selection included (select from end of line)
      highlight = textLayout.getLogicalHighlightShape(hit.getCharIndex(), textLayout.getCharacterCount());
      highlightedText.append(textPanel.actualLineTexts.get(lineIndex).substring(hit.getCharIndex(), textLayout.getCharacterCount()));
      firstEndpointDrawn = true;
    }
    return highlight;
  }

  private Shape processMiddleOfLine()
  {
    Shape highlight;// all of selection included (select middle)
    Point origin = new Point(x, (int) y);
    TextHitInfo startHit = computeHit(textLayout, textPanel.startSelectionPoint, origin);
    TextHitInfo endHit = computeHit(textLayout, textPanel.endSelectionPoint, origin);

    int startIndex = startHit.getCharIndex();
    int endIndex = endHit.getCharIndex();

    if (startIndex > endIndex)
    {
      startIndex = endHit.getCharIndex();
      endIndex = startHit.getCharIndex();
    }

    highlight = textLayout.getLogicalHighlightShape(startIndex, endIndex);
    highlightedText.append(textPanel.actualLineTexts.get(lineIndex).substring(startIndex, endIndex));
    firstEndpointDrawn = true;
    lastEndpointDrawn = true;
    return highlight;
  }

  private void drawHighlight(Graphics2D graphics, float y, int x, Shape highlight)
  {
    Graphics2D g = (Graphics2D) graphics.create();
    g.translate(x, y);
    g.setPaint(Color.lightGray);
    g.fill(highlight);
  }

  private TextHitInfo computeHit(TextLayout textLayout, Point point, Point origin)
  {
    float hitX = (float) (point.getX() - origin.x);
    float hitY = (float) (point.getY() - origin.x);
    return textLayout.hitTestChar(hitX, hitY);
  }

}
