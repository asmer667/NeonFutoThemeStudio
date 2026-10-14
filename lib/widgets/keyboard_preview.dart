import 'package:flutter/material.dart';
import '../models/theme_project.dart';
import '../models/key_shape.dart';
import 'shape_painter.dart';

class KeyboardPreview extends StatelessWidget {
  final ThemeProject project;
  final KeyShape shape;

  const KeyboardPreview({
    super.key,
    required this.project,
    required this.shape,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: project.keyboardSurface,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _key('ض', project.keyboardContainer),
              _spacer(),
              _key('ص', project.keyboardContainer),
              _spacer(),
              _key('ث', project.keyboardContainer),
              _spacer(),
              _key('ق', project.keyboardContainer),
              _spacer(),
              _key('ف', project.keyboardContainer),
            ],
          ),
          _gap(),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _key('ش', project.keyboardContainer),
              _spacer(),
              _key('س', project.keyboardContainer),
              _spacer(),
              _key('ي', project.keyboardContainer),
              _spacer(),
              _key('ب', project.keyboardContainer),
              _spacer(),
              _key('ل', project.keyboardContainer),
            ],
          ),
          _gap(),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _keyIcon(Icons.keyboard_arrow_up, project.keyboardContainer),
              _spacer(),
              _key('ا', project.keyboardContainer),
              _spacer(),
              _key('ت', project.keyboardPress),
              _spacer(),
              _key('ن', project.keyboardContainer),
              _spacer(),
              _key('م', project.keyboardContainer),
              _spacer(),
              _keyIcon(Icons.backspace_outlined, project.keyboardContainer),
            ],
          ),
          _gap(),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _key('?123', project.keyboardContainer, wide: 1.4),
              _spacer(),
              _key('،', project.keyboardContainer),
              _spacer(),
              _keyIcon(Icons.emoji_emotions_outlined, project.keyboardContainer),
              _spacer(),
              _key('مسافة', project.keyboardContainer, wide: 4),
              _spacer(),
              _key('.', project.keyboardContainer),
              _spacer(),
              _keyIcon(Icons.keyboard_return, project.keyboardContainer),
            ],
          ),
        ],
      ),
    );
  }

  Widget _spacer() => const SizedBox(width: 4);
  Widget _gap() => const SizedBox(height: 6);

  Widget _key(String label, Color bg, {double wide = 1}) {
    return ShapeWidget(
      shape: shape,
      color: bg,
      borderColor: project.surface,
      roundedness: project.roundedness,
      width: 44 * wide,
      height: 48,
      child: Text(
        label,
        style: TextStyle(
          color: project.onSurface,
          fontSize: 14,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  Widget _keyIcon(IconData icon, Color bg) {
    return ShapeWidget(
      shape: shape,
      color: bg,
      borderColor: project.surface,
      roundedness: project.roundedness,
      width: 48,
      height: 48,
      child: Icon(icon, color: project.onSurface, size: 18),
    );
  }
}
