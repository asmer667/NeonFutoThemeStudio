import 'package:flutter/material.dart';
import 'screens/studio_shell.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const UltimateThemeCreatorApp());
}

class UltimateThemeCreatorApp extends StatelessWidget {
  const UltimateThemeCreatorApp({super.key});

  @override
  Widget build(BuildContext context) => const StudioShell();
}
