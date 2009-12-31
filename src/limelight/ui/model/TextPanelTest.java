//- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import junit.framework.TestCase;
import limelight.styles.Style;
import limelight.styles.FlatStyle;
import limelight.ui.api.MockScene;

import javax.swing.*;
import java.awt.font.TextLayout;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class TextPanelTest extends TestCase
{
  private TextPanel panel;
  private Style style;
  private JFrame frame;
  private MockPropablePanel parent;
  private RootPanel root;
  private String defaultFontFace;
  private String defaultFontSize;
  private String defaultFontStyle;
  private Color defaultTextColor;

  public void setUp() throws Exception
  {
    parent = new MockPropablePanel();
    parent.setLocation(0, 0);
    parent.setSize(100, 100);
    style = parent.getProp().getStyle();
    panel = new TextPanel(parent, "Some Text");
    parent.add(panel);
    root = new RootPanel(new MockPropFrame());
    root.setPanel(parent);
    style.setTextColor("black");

    defaultFontFace = style.getFontFace();
    defaultFontSize = style.getFontSize();
    defaultFontStyle = style.getFontStyle();
    defaultTextColor = style.getCompiledTextColor().getColor();
  }

  public void tearDown()
  {
    if(frame != null)
      frame.setVisible(false);
  }

  public void testConstructor() throws Exception
  {
    assertEquals(parent, panel.getPanel());
    assertEquals("Some Text", panel.getText());
  }

  public void testPreferredSize() throws Exception
  {
    useFrame();
    panel.doLayout();
    assertEquals(59, panel.getWidth());
    assertEquals(14, panel.getHeight());
  }

  public void testPreferredSizeWithMoreText() throws Exception
  {
    useFrame();
    panel.setText("Once upon a time, there was a developer working on a tool called Limelight.");
    panel.doLayout();
    assertEquals(true, panel.getWidth() >= 98 && panel.getWidth() <= 99);
    assertEquals(69, panel.getHeight());
  }

  public void testPreferredSizeWithBigFontSize() throws Exception
  {
    useFrame();
    style.setFontSize("40");
    panel.doLayout();
    assertEquals(80, panel.getWidth());
    assertEquals(138, panel.getHeight());
  }

  public void testDimensionsWhenLastLineIsLongest() throws Exception
  {
    useFrame();
    panel.setText("1\n2\n3\nlongest");
    panel.doLayout();
    assertEquals(true, panel.getWidth() >= 39 && panel.getWidth() <= 41);
    assertEquals(55, panel.getHeight());
  }

  private void useFrame()
  {
    frame = new JFrame();
    frame.setVisible(true);
    panel.setGraphics(frame.getGraphics());
  }

  public void testTextChanged() throws Exception
  {
    assertFalse(panel.textChanged());

    panel.setText("Something");
    assertTrue(panel.needsLayout());

    panel.resetLayout();
    panel.setText("Something");
    assertFalse(panel.needsLayout());

    panel.setText("Something Else");
    assertTrue(panel.needsLayout());

    panel.resetLayout();
    assertFalse(panel.needsLayout());
  }
  
  public void testLayoutFlushedChangedText() throws Exception
  {
    panel.resetLayout();
    assertEquals(false, panel.needsLayout());

    panel.setText("Something");
    assertEquals(true, panel.needsLayout());

    panel.doLayout();
    assertEquals(false, panel.needsLayout());
  }

  public void testCanBeBuffered() throws Exception
  {
    assertEquals(false, panel.canBeBuffered());
  }

  public void testBuildingLines() throws Exception
  {
    panel.setText("some text");
    panel.buildLines();

    List<TextLayout> lines = panel.getLines();

    assertEquals(1, lines.size());
    TextLayout layout = lines.get(0);
    assertEquals(10, layout.getCharacterCount());
    assertSubString("family=" + defaultFontFace, layout.toString());
    assertSubString("name=" + defaultFontFace, layout.toString());
    assertSubString("size=" + defaultFontSize, layout.toString());
  }

  public void testStylingAppliedToLine() throws Exception
  {
    createStyles();

    parent.setSize(200, 100);
    panel.setText("<my_style>some text</my_style>");
    panel.buildLines();


    List<TextLayout> lines = panel.getLines();

    TextLayout layout = lines.get(0);
    assertEquals(1, lines.size());
    assertEquals(10, layout.getCharacterCount());
    assertSubString("family=Helvetica", layout.toString());
    assertSubString("name=Helvetica", layout.toString());
    assertSubString("style=bold", layout.toString());
    assertSubString("size=20", layout.toString());
  }

  public void testObserverAddedForLineStyling() throws Exception
  {
    createStyles();

    panel.setText("<my_style>some text</my_style>");
    panel.buildLines();

    Style myStyle = panel.getStyleFromTag("my_style");
    assertEquals(true, myStyle.hasObserver(panel));
  }

  public void testMultipleStylesAppliedToLine() throws Exception
  {
    createStyles();

    parent.setSize(200, 100);
    panel.setText("<my_style>some </my_style><my_other_style>text</my_other_style>");
    panel.buildLines();

    List<TextPanel.StyledString> chunks = panel.getTextChunks();

    TextPanel.StyledString layout = chunks.get(0);
    assertEquals(5, layout.getCharacterCount());
    assertSubString("family=Helvetica", layout.toString());
    assertSubString("name=Helvetica", layout.toString());
    assertSubString("style=bold", layout.toString());
    assertSubString("size=20", layout.toString());

    TextPanel.StyledString layout2 = chunks.get(1);
    assertEquals(5, layout.getCharacterCount());
    assertSubString("family=Dialog", layout2.toString());
    assertSubString("name=Cuneiform", layout2.toString());
    assertSubString("style=italic", layout2.toString());
    assertSubString("size=19", layout2.toString());
  }

  private void createStyles()
  {
    parent.prop.scene = new MockScene();
    FlatStyle myStyle = new FlatStyle();
    ((MockScene)parent.prop.scene).styles.put("my_style", myStyle);
    myStyle.setFontFace("Helvetica");
    myStyle.setFontStyle("bold");
    myStyle.setFontSize("20");
    myStyle.setTextColor("red");

    FlatStyle myOtherStyle = new FlatStyle();
    ((MockScene)parent.prop.scene).styles.put("my_other_style", myOtherStyle);
    myOtherStyle.setFontFace("Cuneiform");
    myOtherStyle.setFontStyle("italic");
    myOtherStyle.setFontSize("19");
    myOtherStyle.setTextColor("blue");
  }

  public void testInterlacedTextAndStyledText()
  {
    createStyles();
    parent.setSize(200, 100);
    panel.setText("This is <my_other_style>some </my_other_style> fantastic <my_style>text</my_style>");
    panel.buildLines();

    List<TextPanel.StyledString> chunks = panel.getTextChunks();
    assertEquals(5, chunks.size());

    TextPanel.StyledString interlacedLayout = chunks.get(2);
    assertNoSubString("name=Cuneiform", interlacedLayout.toString());
    assertNoSubString("size=19", interlacedLayout.toString());
  }

  public void testUnrecognizedInterlacedStyle()
  {
    createStyles();
    parent.setSize(200, 100);
    panel.setText("This is <my_other_style>some </my_other_style><bogus_style>fantastic</bogus_style><my_style>text</my_style>");
    panel.buildLines();

    List<TextPanel.StyledString> chunks = panel.getTextChunks();
    assertEquals(5, chunks.size());

    TextPanel.StyledString interlacedLayout = chunks.get(2);
    assertNoSubString("name=Cuneiform", interlacedLayout.toString());
    assertNoSubString("size=19", interlacedLayout.toString());
  }

  public void testStyledTextOnSameLine()
  {
    createStyles();
    parent.setSize(200, 100);
    panel.setText("This <my_other_style>some </my_other_style> text");
    panel.buildLines();

    List<TextLayout> lines = panel.getLines();
    assertEquals(1, lines.size());

    String onlyLine = lines.get(0).toString();
    assertSubString("name=Cuneiform", onlyLine);
    assertSubString("size=19", onlyLine);
    assertSubString("style=italic", onlyLine);
    assertSubString("name=" + defaultFontFace, onlyLine);
    assertSubString("size=" + defaultFontSize, onlyLine);
    assertSubString("style=" + defaultFontStyle, onlyLine);
  }

  public void testStyledAcrossLineBreak()
  {
    createStyles();
    parent.setSize(200, 100);
    panel.setText("This <my_other_style>some\n more</my_other_style> text");

    panel.buildLines();

    List<TextLayout> lines = panel.getLines();
    assertEquals(2, lines.size());

    TextLayout first = lines.get(0);
    TextLayout second = lines.get(1);
    assertSubString("name=Cuneiform", first.toString());
    assertSubString("name=" + defaultFontFace, first.toString());
    assertSubString("name=Cuneiform", second.toString());
    assertSubString("name=" + defaultFontFace, second.toString());
  }

  public void testTextColor() throws Exception
  {
    createStyles();
    panel.setText("text <my_other_style>here</my_other_style> man");
    panel.buildLines();

    TextPanel.StyledString first = panel.getTextChunks().get(0);
    assertEquals(defaultTextColor, first.color);

    TextPanel.StyledString second = panel.getTextChunks().get(1);
    assertEquals(new Color(0x0000FF), second.color);

    TextPanel.StyledString third = panel.getTextChunks().get(2);
    assertEquals(defaultTextColor, third.color);
  }

  public void testTextChunksOverwrittenOnCompile() throws Exception
  {
    panel.setText("Here is some original text.");
    panel.buildLines();

    int originalChunks = panel.getTextChunks().size();

    panel.buildLines();

    assertEquals(originalChunks, panel.getTextChunks().size());
  }

  private void assertSubString(String subString, String fullString)
  {
    int i = fullString.indexOf(subString);
    assertTrue(subString + " not found in " + fullString, i > -1);
  }

  private void assertNoSubString(String subString, String fullString)
  {
    int i = fullString.indexOf(subString);
    assertFalse(subString + " found in " + fullString, i > -1);
  }

  public void testChangingTestRequiresUpdates() throws Exception
  {
    parent.doLayout();
    assertFalse(panel.needsLayout());
    assertFalse(parent.needsLayout());

    panel.setText("New Text");

    assertEquals(true, panel.needsLayout());
    assertEquals(true, parent.needsLayout());        
  }

  public void testLayoutCausesDirtyRegion() throws Exception
  {
    panel.doLayout();

    ArrayList<Rectangle> list = new ArrayList<Rectangle>();
    root.getAndClearDirtyRegions(list);
    assertEquals(1, list.size());
    assertEquals(panel.getAbsoluteBounds(), list.get(0));
  }
  
  public void testResizesTextWhenSizeChanges() throws Exception
  {
    panel.setText("Some really long text so that there are multiple lines requiring layout when the size changes.");
    panel.doLayout();

    int originalHeight = panel.getHeight();
    parent.setSize(400, 200);
    panel.doLayout();

    int newHeight = panel.getHeight();

    assertEquals(true, 200 - panel.getWidth() < 100 );
    assertEquals(true, newHeight < originalHeight);
  }
  
  public void testParentSizeChangesAlwaysRequiresLayout() throws Exception
  {
    panel.resetLayout();
    assertEquals(false, panel.needsLayout());

    panel.consumableAreaChanged();

    assertEquals(true, panel.needsLayout());
  }
}


