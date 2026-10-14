import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'dart:ui' as ui;

class AssetGenerator {
  /// يولّد صورة زر بحجم 128x140
  static Future<Uint8List> generateButtonPng({
    required Color bgColor,
    required Color borderColor,
    double radius = 20,
    int width = 128,
    int height = 140,
  }) async {
    final recorder = ui.PictureRecorder();
    final canvas = Canvas(recorder);
    final paint = Paint()..color = bgColor..style = PaintingStyle.fill;
    final borderPaint = Paint()
      ..color = borderColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;

    final rect = Rect.fromLTWH(2, 2, width - 4.0, height - 4.0);
    final rrect = RRect.fromRectAndRadius(rect, Radius.circular(radius));

    canvas.drawRRect(rrect, paint);
    canvas.drawRRect(rrect, borderPaint);

    final picture = recorder.endRecording();
    final img = await picture.toImage(width, height);
    final byteData = await img.toByteData(format: ui.ImageByteFormat.png);
    return byteData!.buffer.asUint8List();
  }

  /// يولّد أيقونة بحجم 64x64
  static Future<Uint8List> generateIconPng({
    required IconData icon,
    int size = 64,
  }) async {
    final recorder = ui.PictureRecorder();
    final canvas = Canvas(recorder);

    final iconPainter = TextPainter(
      textDirection: TextDirection.ltr,
      text: TextSpan(
        text: String.fromCharCode(icon.codePoint),
        style: TextStyle(
          fontSize: size * 0.7,
          fontFamily: icon.fontFamily,
          package: icon.fontPackage,
          color: Colors.white,
        ),
      ),
    )..layout();

    iconPainter.paint(
      canvas,
      Offset((size - iconPainter.width) / 2, (size - iconPainter.height) / 2),
    );

    final picture = recorder.endRecording();
    final img = await picture.toImage(size, size);
    final byteData = await img.toByteData(format: ui.ImageByteFormat.png);
    return byteData!.buffer.asUint8List();
  }

  /// يكتب ملف صورة في مجلد مؤقت ويعيد الملف
  static Future<File> writePng(String name, Uint8List bytes, Directory dir) async {
    final file = File('${dir.path}/$name');
    await file.writeAsBytes(bytes);
    return file;
  }
}
