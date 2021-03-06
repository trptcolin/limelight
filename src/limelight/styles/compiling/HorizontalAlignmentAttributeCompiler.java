//- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.styles.compiling;

import limelight.styles.abstrstyling.StyleAttributeCompiler;
import limelight.styles.abstrstyling.StyleAttribute;
import limelight.styles.HorizontalAlignment;
import limelight.styles.styling.SimpleHorizontalAlignmentAttribute;

public class HorizontalAlignmentAttributeCompiler extends StyleAttributeCompiler
{
  public StyleAttribute compile(Object value)
  {
    HorizontalAlignment alignment = parse(value);
    if(alignment != null)
      return new SimpleHorizontalAlignmentAttribute(alignment);
    else
      throw makeError(value);
  }

  public static HorizontalAlignment parse(Object value)
  {
    String lowerCase = value.toString().toLowerCase().trim();
    if("left".equals(lowerCase))
      return HorizontalAlignment.LEFT;
    else if("center".equals(lowerCase) || "middle".equals(lowerCase))
      return HorizontalAlignment.CENTER;
    else if("right".equals(lowerCase))
      return HorizontalAlignment.RIGHT;
    else
      return null;
  }
}
