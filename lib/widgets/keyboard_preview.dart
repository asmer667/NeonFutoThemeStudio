import 'package:flutter/material.dart';
import '../models/theme_project.dart';

class KeyboardPreview extends StatelessWidget {
  const KeyboardPreview({super.key, required this.project, this.compact = false});
  final ThemeProject project;
  final bool compact;

  @override
  Widget build(BuildContext context) {
    final p = project;
    final radius = BorderRadius.circular(p.radius);
    final gradient = p.useGradient
        ? LinearGradient(colors: [p.primary, p.secondary], begin: Alignment.centerLeft, end: Alignment.centerRight)
        : null;
    final rows = const [
      ['ض','ص','ث','ق','ف','غ','ع','ه','خ','ح','ج'],
      ['ش','س','ي','ب','ل','ا','ت','ن','م','ك','ط'],
      ['ذ','ئ','ء','ؤ','ر','ى','ة','و','ز','ظ','د'],
    ];
    return Container(
      decoration: BoxDecoration(
        color: p.background,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: p.outline.withValues(alpha: .65)),
      ),
      padding: EdgeInsets.all(compact ? 6 : 10),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (p.showActionRow)
            Row(
              children: [
                _tool(Icons.more_horiz, p.functionKey, p.keyText),
                _tool(Icons.language, p.functionKey, p.keyText),
                _tool(Icons.undo, p.functionKey, p.keyText),
                _tool(Icons.redo, p.functionKey, p.keyText),
                _tool(Icons.edit, p.functionKey, p.keyText),
                _tool(Icons.content_paste, p.functionKey, p.keyText),
              ],
            ),
          if (p.showSuggestionRow)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 5),
              child: Row(
                children: ['مرحبا', 'كيف حالك', 'اقتراحات'].map((s) => Expanded(
                  child: Container(
                    margin: const EdgeInsets.symmetric(horizontal: 2),
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    decoration: BoxDecoration(color: p.keyColor, borderRadius: BorderRadius.circular(8)),
                    child: Text(s, textAlign: TextAlign.center, maxLines: 1,
                      style: TextStyle(color: p.keyText, fontSize: compact ? 8 : 10)),
                  ),
                )).toList(),
              ),
            ),
          for (final row in rows)
            Padding(
              padding: EdgeInsets.only(top: p.keyGap, bottom: p.keyGap),
              child: Row(
                children: row.map((letter) => Expanded(
                  child: Container(
                    margin: EdgeInsets.symmetric(horizontal: p.keyGap / 2),
                    height: compact ? 26 : 34,
                    alignment: Alignment.center,
                    decoration: BoxDecoration(
                      color: p.keyColor,
                      gradient: p.useGradient ? LinearGradient(
                        colors: [p.keyColor, Color.lerp(p.keyColor, p.primary, .28)!],
                        begin: Alignment.topLeft, end: Alignment.bottomRight,
                      ) : null,
                      borderRadius: radius,
                      border: Border.all(color: p.outline.withValues(alpha: .75), width: .7),
                    ),
                    child: FittedBox(
                      child: Text(letter, style: TextStyle(color: p.keyText, fontSize: 17 * p.keyScale, fontFamily: p.fontName)),
                    ),
                  ),
                )).toList(),
              ),
            ),
          Row(
            children: [
              _wideKey('؟123', p.functionKey, p.keyText, .85),
              _wideKey('،', p.keyColor, p.keyText, .45),
              _wideKey('🌐', p.functionKey, p.keyText, .5),
              _wideKey('العربية', p.primary, p.keyText, 2.4, gradient: gradient),
              _wideKey('.', p.keyColor, p.keyText, .45),
              _wideKey('↵', p.functionKey, p.keyText, .7),
            ],
          ),
        ],
      ),
    );
  }

  Widget _tool(IconData icon, Color bg, Color fg) => Expanded(
    child: Container(
      margin: const EdgeInsets.all(2),
      height: compact ? 22 : 28,
      decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(7)),
      child: Icon(icon, size: compact ? 12 : 15, color: fg),
    ),
  );

  Widget _wideKey(String text, Color bg, Color fg, double flex, {Gradient? gradient}) => Expanded(
    flex: (flex * 10).round(),
    child: Container(
      height: compact ? 28 : 38,
      margin: const EdgeInsets.all(2),
      alignment: Alignment.center,
      decoration: BoxDecoration(color: bg, gradient: gradient, borderRadius: BorderRadius.circular(8),
        border: Border.all(color: project.outline.withValues(alpha: .8))),
      child: FittedBox(child: Text(text, style: TextStyle(color: fg, fontSize: 12))),
    ),
  );
}
