"use client";

import Editor, { OnMount } from "@monaco-editor/react";
import type { editor } from "monaco-editor";

type Props = {
  value: string;
  language: string;
  readOnly?: boolean;
  onChange: (value: string) => void;
  onCursor: (line: number, column: number) => void;
};

export function CodeEditor({ value, language, readOnly, onChange, onCursor }: Props) {
  const onMount: OnMount = (instance: editor.IStandaloneCodeEditor) => {
    instance.onDidChangeCursorPosition((event) => {
      onCursor(event.position.lineNumber, event.position.column);
    });
  };

  return (
    <Editor
      height="100%"
      theme="vs-dark"
      language={language}
      value={value}
      onChange={(next) => onChange(next ?? "")}
      onMount={onMount}
      options={{
        readOnly,
        minimap: { enabled: false },
        fontSize: 14,
        lineHeight: 23,
        padding: { top: 14 },
        smoothScrolling: true,
        cursorSmoothCaretAnimation: "on",
        automaticLayout: true
      }}
    />
  );
}
