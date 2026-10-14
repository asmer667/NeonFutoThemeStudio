import 'package:flutter_test/flutter_test.dart';
import 'package:ultimate_theme_creator_studio/models/theme_project.dart';

void main() {
  test('theme project serializes and restores core values', () {
    final original = ThemeProject(name: 'Test Theme', author: 'Tester');
    final restored = ThemeProject.fromJson(original.toJson());
    expect(restored.name, 'Test Theme');
    expect(restored.author, 'Tester');
    expect(restored.primary.value, original.primary.value);
    expect(restored.showSuggestionRow, isTrue);
  });
}
