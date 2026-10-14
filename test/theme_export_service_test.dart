import 'package:flutter_test/flutter_test.dart';
import 'package:ultimate_theme_creator_studio/models/theme_project.dart';
import 'package:ultimate_theme_creator_studio/services/theme_export_service.dart';

void main() {
  test('generated theme contains FUTO sections and selectors', () {
    final service = ThemeExportService();
    final text = service.generateThemeTxt(ThemeProject(name: 'Unit Test'));
    expect(text, contains('[options]'));
    expect(text, contains('[colors]'));
    expect(text, contains('[[matchrules.border]]'));
    expect(text, contains('selector = "spacebar"'));
    expect(text, contains('[[asset.icon]]'));
  });
}
