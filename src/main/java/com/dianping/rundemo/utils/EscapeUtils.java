package com.dianping.rundemo.utils;

public class EscapeUtils {

   /**
    * 该方法来自hudson的“hudson.Util.escape(String)” <br>
    * hudson的控制台页面是输出html代码，然后直接e.html(htmlContent)显示，所以hudson的escape实际是转化普通文本为html，
    * 而不是escapeHtml <br>
    * <br>
    * 不过我的页面输出： <br>
    * (1)直接通过模板显示的，已经通过velocity配置转义(会把中文也转义)<br>
    * (2)通过json输出时，jquery.text()显示已经作了html转义<br>
    * 所以我不需要hudson的这个方法。 <br>
    * <br>
    * Escapes HTML unsafe characters like &lt;, &amp; to the respective
    * character entities.
    */
   public static String escapeHtml(String text) {
      if (text == null)
         return null;
      StringBuilder buf = new StringBuilder(text.length() + 64);
      for (int i = 0; i < text.length(); i++) {
         char ch = text.charAt(i);
         if (ch == '<')
            buf.append("&lt;");
         if (ch == '\n')
            buf.append("<br>");
         else if (ch == '<')
            buf.append("&lt;");
         else if (ch == '&')
            buf.append("&amp;");
         else if (ch == '"')
            buf.append("&quot;");
         else if (ch == '\'')
            buf.append("&#039;");
         else if (ch == ' ') {
            // All spaces in a block of consecutive spaces are converted to
            // non-breaking space (&nbsp;) except for the last one.  This allows
            // significant whitespace to be retained without prohibiting wrapping.
            char nextCh = i + 1 < text.length() ? text.charAt(i + 1) : 0;
            buf.append(nextCh == ' ' ? "&nbsp;" : " ");
         } else
            buf.append(ch);
      }
      return buf.toString();
   }
}
