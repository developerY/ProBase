#!/bin/bash
sed -i '' 's/grep -n -i "health"/grep -n -i "android\\.permission\\.health\\."/g' applications/kocolor/docs/ProductFixes/Final_steps_Audit.md
