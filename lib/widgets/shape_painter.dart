import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../models/key_shape.dart';

class ShapePainter extends CustomPainter {
  final KeyShape shape;
  final Color color;
  final Color borderColor;
  final double roundedness;

  ShapePainter({
    required this.shape,
    required this.color,
    required this.borderColor,
    this.roundedness = 0.5,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final fill = Paint()
      ..color = color
      ..style = PaintingStyle.fill
      ..isAntiAlias = true;
    final border = Paint()
      ..color = borderColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2
      ..isAntiAlias = true;

    final path = _buildPath(size);

    canvas.drawPath(path, fill);
    canvas.drawPath(path, border);
  }

  Path _buildPath(Size size) {
    final w = size.width;
    final h = size.height;
    final path = Path();

    switch (shape) {
      case KeyShape.rectangle:
        path.addRect(Rect.fromLTWH(0, 0, w, h));
        break;

      case KeyShape.roundedRectangle:
        final r = (math.min(w, h) * 0.25) * roundedness;
        path.addRRect(
          RRect.fromRectAndRadius(Rect.fromLTWH(0, 0, w, h), Radius.circular(r)),
        );
        break;

      case KeyShape.circle:
        path.addOval(Rect.fromLTWH(0, 0, w, h));
        break;

      case KeyShape.hexagon:
        final r = math.min(w, h) / 2;
        final cx = w / 2;
        final cy = h / 2;
        for (int i = 0; i < 6; i++) {
          final angle = (math.pi / 3) * i - math.pi / 6;
          final x = cx + r * math.cos(angle);
          final y = cy + r * math.sin(angle);
          if (i == 0) {
            path.moveTo(x, y);
          } else {
            path.lineTo(x, y);
          }
        }
        path.close();
        break;

      case KeyShape.triangle:
        path.moveTo(w / 2, 0);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        break;

      case KeyShape.star:
        final cx = w / 2;
        final cy = h / 2;
        final outerR = math.min(w, h) / 2;
        final innerR = outerR * 0.5;
        for (int i = 0; i < 10; i++) {
          final r = (i % 2 == 0) ? outerR : innerR;
          final angle = (math.pi / 5) * i - math.pi / 2;
          final x = cx + r * math.cos(angle);
          final y = cy + r * math.sin(angle);
          if (i == 0) {
            path.moveTo(x, y);
          } else {
            path.lineTo(x, y);
          }
        }
        path.close();
        break;

      case KeyShape.diamond:
        path.moveTo(w / 2, 0);
        path.lineTo(w, h / 2);
        path.lineTo(w / 2, h);
        path.lineTo(0, h / 2);
        path.close();
        break;

      case KeyShape.pentagon:
        final cx = w / 2;
        final cy = h / 2;
        final r = math.min(w, h) / 2;
        for (int i = 0; i < 5; i++) {
          final angle = (2 * math.pi / 5) * i - math.pi / 2;
          final x = cx + r * math.cos(angle);
          final y = cy + r * math.sin(angle);
          if (i == 0) {
            path.moveTo(x, y);
          } else {
            path.lineTo(x, y);
          }
        }
        path.close();
        break;

      case KeyShape.octagon:
        final cx = w / 2;
        final cy = h / 2;
        final r = math.min(w, h) / 2;
        for (int i = 0; i < 8; i++) {
          final angle = (math.pi / 4) * i - math.pi / 8;
          final x = cx + r * math.cos(angle);
          final y = cy + r * math.sin(angle);
          if (i == 0) {
            path.moveTo(x, y);
          } else {
            path.lineTo(x, y);
          }
        }
        path.close();
        break;

      case KeyShape.heart:
        path.moveTo(w / 2, h);
        path.cubicTo(-w * 0.2, h * 0.55, 0, 0, w / 2, h * 0.3);
        path.cubicTo(w, 0, w * 1.2, h * 0.55, w / 2, h);
        path.close();
        break;

      case KeyShape.custom:
      default:
        final r = (math.min(w, h) * 0.25) * roundedness;
        path.addRRect(
          RRect.fromRectAndRadius(Rect.fromLTWH(0, 0, w, h), Radius.circular(r)),
        );
    }

    return path;
  }

  @override
  bool shouldRepaint(covariant ShapePainter oldDelegate) {
    return oldDelegate.shape != shape ||
        oldDelegate.color != color ||
        oldDelegate.borderColor != borderColor ||
        oldDelegate.roundedness != roundedness;
  }
}

class ShapeWidget extends StatelessWidget {
  final KeyShape shape;
  final Color color;
  final Color borderColor;
  final double roundedness;
  final Widget? child;
  final double width;
  final double height;

  const ShapeWidget({
    super.key,
    required this.shape,
    required this.color,
    required this.borderColor,
    this.roundedness = 0.5,
    this.child,
    this.width = 48,
    this.height = 48,
  });

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: Size(width, height),
      painter: ShapePainter(
        shape: shape,
        color: color,
        borderColor: borderColor,
        roundedness: roundedness,
      ),
      child: SizedBox(
        width: width,
        height: height,
        child: Center(child: child),
      ),
    );
  }
}
