import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/theme_project.dart';

class ProjectStore {
  static const _key = 'saved_theme_projects_v1';

  Future<List<ThemeProject>> loadAll() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_key);
    if (raw == null || raw.isEmpty) return [ThemeProject(name: 'Aurora Glass')];
    try {
      final decoded = jsonDecode(raw) as List<dynamic>;
      return decoded
          .map((item) => ThemeProject.fromJson(Map<String, dynamic>.from(item as Map)))
          .toList();
    } catch (_) {
      return [ThemeProject(name: 'Aurora Glass')];
    }
  }

  Future<void> saveAll(List<ThemeProject> projects) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, jsonEncode(projects.map((p) => p.toJson()).toList()));
  }
}
