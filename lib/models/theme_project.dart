import 'package:flutter/material.dart';

class ThemeProject {
  String id;
  String name;
  String author;
  String description;
  Color primary;
  Color onPrimary;
  Color secondary;
  Color surface;
  Color onSurface;
  Color keyboardSurface;
  Color keyboardContainer;
  Color keyboardPress;
  double roundedness;
  double scaleText;
  double scaleHints;

  ThemeProject({
    required this.id,
    required this.name,
    required this.author,
    required this.description,
    required this.primary,
    required this.onPrimary,
    required this.secondary,
    required this.surface,
    required this.onSurface,
    required this.keyboardSurface,
    required this.keyboardContainer,
    required this.keyboardPress,
    required this.roundedness,
    required this.scaleText,
    required this.scaleHints,
  });

  factory ThemeProject.createDefault() {
    return ThemeProject(
      id: 'studio.${DateTime.now().millisecondsSinceEpoch}',
      name: 'My Theme',
      author: 'Anonymous',
      description: 'A theme created with FUTO Theme Studio.',
      primary: const Color(0xFF16D9F2),
      onPrimary: const Color(0xFFFFFFFF),
      secondary: const Color(0xFF8B5CF6),
      surface: const Color(0xFF071426),
      onSurface: const Color(0xFFF4FAFF),
      keyboardSurface: const Color(0xFF071426),
      keyboardContainer: const Color(0xFF102C43),
      keyboardPress: const Color(0xFF16D9F2),
      roundedness: 0.5,
      scaleText: 1.0,
      scaleHints: 0.9,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'author': author,
        'description': description,
        'primary': primary.toARGB32(),
        'onPrimary': onPrimary.toARGB32(),
        'secondary': secondary.toARGB32(),
        'surface': surface.toARGB32(),
        'onSurface': onSurface.toARGB32(),
        'keyboardSurface': keyboardSurface.toARGB32(),
        'keyboardContainer': keyboardContainer.toARGB32(),
        'keyboardPress': keyboardPress.toARGB32(),
        'roundedness': roundedness,
        'scaleText': scaleText,
        'scaleHints': scaleHints,
      };

  factory ThemeProject.fromJson(Map<String, dynamic> json) {
    return ThemeProject(
      id: json['id'] as String,
      name: json['name'] as String,
      author: json['author'] as String,
      description: json['description'] as String,
      primary: Color(json['primary'] as int),
      onPrimary: Color(json['onPrimary'] as int),
      secondary: Color(json['secondary'] as int),
      surface: Color(json['surface'] as int),
      onSurface: Color(json['onSurface'] as int),
      keyboardSurface: Color(json['keyboardSurface'] as int),
      keyboardContainer: Color(json['keyboardContainer'] as int),
      keyboardPress: Color(json['keyboardPress'] as int),
      roundedness: (json['roundedness'] as num).toDouble(),
      scaleText: (json['scaleText'] as num).toDouble(),
      scaleHints: (json['scaleHints'] as num).toDouble(),
    );
  }
}
