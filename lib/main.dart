import 'package:flutter/material.dart';
import 'screens/editor_screen.dart';

void main() {
  runApp(const FutoThemeStudioApp());
}

class FutoThemeStudioApp extends StatelessWidget {
  const FutoThemeStudioApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FUTO Theme Studio',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF16D9F2),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: const EditorScreen(),
    );
  }
}
