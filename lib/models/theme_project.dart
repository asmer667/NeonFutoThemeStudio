import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';

class ThemeProject {
  ThemeProject({
    String? id,
    this.name = 'Aurora Glass',
    this.author = 'Theme Studio',
    this.description = 'A custom theme created with Ultimate Theme Creator Studio.',
    this.version = 1,
    this.primary = const Color(0xFF16D9F2),
    this.secondary = const Color(0xFF8B5CF6),
    this.background = const Color(0xFF071426),
    this.keyColor = const Color(0xFF102C43),
    this.keyText = const Color(0xFFF4FAFF),
    this.functionKey = const Color(0xFF183D58),
    this.outline = const Color(0xFF327D9D),
    this.radius = 12,
    this.keyGap = 4,
    this.keyScale = 1.0,
    this.backgroundName = 'Midnight',
    this.fontName = 'Roboto',
    this.layoutName = 'Arabic',
    this.gradientName = 'Aurora',
    this.shapeName = 'Rounded',
    this.language = 'Arabic',
    this.showSuggestionRow = true,
    this.showActionRow = true,
    this.useGradient = true,
    this.useBackgroundImage = false,
  }) : id = id ?? const Uuid().v4();

  final String id;
  String name;
  String author;
  String description;
  int version;
  Color primary;
  Color secondary;
  Color background;
  Color keyColor;
  Color keyText;
  Color functionKey;
  Color outline;
  double radius;
  double keyGap;
  double keyScale;
  String backgroundName;
  String fontName;
  String layoutName;
  String gradientName;
  String shapeName;
  String language;
  bool showSuggestionRow;
  bool showActionRow;
  bool useGradient;
  bool useBackgroundImage;

  ThemeProject copy() => ThemeProject.fromJson(toJson());

  Map<String, dynamic> toJson() => {
    'id': id,
    'name': name,
    'author': author,
    'description': description,
    'version': version,
    'primary': primary.value,
    'secondary': secondary.value,
    'background': background.value,
    'keyColor': keyColor.value,
    'keyText': keyText.value,
    'functionKey': functionKey.value,
    'outline': outline.value,
    'radius': radius,
    'keyGap': keyGap,
    'keyScale': keyScale,
    'backgroundName': backgroundName,
    'fontName': fontName,
    'layoutName': layoutName,
    'gradientName': gradientName,
    'shapeName': shapeName,
    'language': language,
    'showSuggestionRow': showSuggestionRow,
    'showActionRow': showActionRow,
    'useGradient': useGradient,
    'useBackgroundImage': useBackgroundImage,
  };

  factory ThemeProject.fromJson(Map<String, dynamic> json) => ThemeProject(
    id: json['id'] as String?,
    name: json['name'] as String? ?? 'Untitled Theme',
    author: json['author'] as String? ?? 'Theme Studio',
    description: json['description'] as String? ?? '',
    version: json['version'] as int? ?? 1,
    primary: Color(json['primary'] as int? ?? 0xFF16D9F2),
    secondary: Color(json['secondary'] as int? ?? 0xFF8B5CF6),
    background: Color(json['background'] as int? ?? 0xFF071426),
    keyColor: Color(json['keyColor'] as int? ?? 0xFF102C43),
    keyText: Color(json['keyText'] as int? ?? 0xFFF4FAFF),
    functionKey: Color(json['functionKey'] as int? ?? 0xFF183D58),
    outline: Color(json['outline'] as int? ?? 0xFF327D9D),
    radius: (json['radius'] as num? ?? 12).toDouble(),
    keyGap: (json['keyGap'] as num? ?? 4).toDouble(),
    keyScale: (json['keyScale'] as num? ?? 1).toDouble(),
    backgroundName: json['backgroundName'] as String? ?? 'Midnight',
    fontName: json['fontName'] as String? ?? 'Roboto',
    layoutName: json['layoutName'] as String? ?? 'Arabic',
    gradientName: json['gradientName'] as String? ?? 'Aurora',
    shapeName: json['shapeName'] as String? ?? 'Rounded',
    language: json['language'] as String? ?? 'Arabic',
    showSuggestionRow: json['showSuggestionRow'] as bool? ?? true,
    showActionRow: json['showActionRow'] as bool? ?? true,
    useGradient: json['useGradient'] as bool? ?? true,
    useBackgroundImage: json['useBackgroundImage'] as bool? ?? false,
  );

  String toJsonString() => jsonEncode(toJson());

  factory ThemeProject.fromJsonString(String value) =>
      ThemeProject.fromJson(Map<String, dynamic>.from(jsonDecode(value) as Map));

  String colorHex(Color color) =>
      '#${color.value.toRadixString(16).padLeft(8, '0').substring(2).toUpperCase()}';
}
