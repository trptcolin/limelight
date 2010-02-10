package limelight.ui.model;

import limelight.styles.Style;

import java.awt.*;
import java.awt.font.TextLayout;
import java.util.List;

public class TextRenderer
{
  private List<TextLayout> lines;
  private List<String> actualLineTexts;
  
  private Graphics2D graphics;
  private Style style;
  private StringBuffer highlightedText;
  private int width;
  private int height;

  public Point startSelectionPoint;
  public Point endSelectionPoint;

  private int startSelectionIndex;
  private int endSelectionIndex;
  private boolean selectionUpdated;

  public TextRenderer(TextPanel textPanel, Graphics2D graphics)
  {
    init(textPanel, graphics);
  }

  private void init(TextPanel textPanel, Graphics2D graphics)
  {
    this.lines = textPanel.getLines();
    this.actualLineTexts = textPanel.actualLineTexts;

    this.style = textPanel.getStyle();
    this.width = textPanel.getWidth();
    this.height = textPanel.getHeight();

    this.startSelectionPoint = textPanel.startSelectionPoint;
    this.endSelectionPoint = textPanel.endSelectionPoint;

    this.graphics = graphics;
  }


  public void render()
  {
    int y = 0;
    boolean firstEndpointDrawn = false;
    boolean lastEndpointDrawn = false;
    int charactersDrawn = 0;

    highlightedText = new StringBuffer();

    for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++)
    {
      LineProcessor lineProcessor = new LineProcessor(this, graphics, y, firstEndpointDrawn, lastEndpointDrawn, highlightedText, lineIndex, charactersDrawn).invoke();
      firstEndpointDrawn = lineProcessor.isFirstEndpointDrawn();
      lastEndpointDrawn = lineProcessor.isLastEndpointDrawn();
      y = lineProcessor.getY();
      charactersDrawn = lineProcessor.getCharactersDrawn();
    }
  }

  public List<TextLayout> getLines()
  {
    return lines;
  }

  public StringBuffer getHighlightedText()
  {
    return highlightedText;
  }

  public Style getStyle()
  {
    return style;
  }

  public static double widthOf(TextLayout layout)
  {
    return layout.getBounds().getWidth() + layout.getBounds().getX();
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return height;
  }

  public List<String> getActualLineTexts()
  {
    return actualLineTexts;
  }

  public boolean isSelected()
  {
    return endSelectionIndex > 0;
  }

  public void setEndSelectionIndex(int endSelectionIndex)
  {
    this.endSelectionIndex = endSelectionIndex;
  }

  public int getEndSelectionIndex()
  {
    return endSelectionIndex;
  }

  public void setStartSelectionIndex(int startSelectionIndex)
  {
    this.startSelectionIndex = startSelectionIndex;
  }

  public int getStartSelectionIndex()
  {
    return startSelectionIndex;
  }

  public void renderWithSelection(TextPanel textPanel, Graphics2D graphics)
  {
    init(textPanel, graphics);
    
    int y = 0;
    boolean firstEndpointDrawn = false;
    boolean lastEndpointDrawn = false;
    int charactersDrawn = 0;

    highlightedText = new StringBuffer();

    for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++)
    {
      LineProcessor lineProcessor = new LineSelectionProcessor(this, graphics, y, firstEndpointDrawn, lastEndpointDrawn, highlightedText, lineIndex, charactersDrawn).invoke();
      firstEndpointDrawn = lineProcessor.isFirstEndpointDrawn();
      lastEndpointDrawn = lineProcessor.isLastEndpointDrawn();
      y = lineProcessor.getY();
      charactersDrawn = lineProcessor.getCharactersDrawn();
    }
  }

  public void updateSelection()
  {
    setSelectionUpdated(true);
  }

  public boolean isSelectionUpdated()
  {
    return selectionUpdated;
  }

  public void setSelectionUpdated(boolean selectionUpdated)
  {
    this.selectionUpdated = selectionUpdated;
  }
}
