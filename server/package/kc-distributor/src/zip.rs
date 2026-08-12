use anyhow::{Context, Result};
use std::fs::File;
use std::io::{self, Read, Write};
use std::path::Path;
use walkdir::WalkDir;
use zip::write::FileOptions;
use zip::{CompressionMethod, ZipWriter};

pub fn create_deployment_zip(
    source_dir: impl AsRef<Path>,
    output_file: impl AsRef<Path>,
) -> Result<()> {
    let source_dir = source_dir.as_ref();
    let output_file = output_file.as_ref();

    if !source_dir.exists() {
        anyhow::bail!(
            "Deployment source directory does not exist: {}",
            source_dir.display()
        );
    }

    if let Some(parent) = output_file.parent() {
        std::fs::create_dir_all(parent)?;
    }

    let file = File::create(output_file)
        .with_context(|| format!("Unable to create {}", output_file.display()))?;

    let mut zip = ZipWriter::new(file);

    let options = FileOptions::default()
        .compression_method(CompressionMethod::Deflated)
        .unix_permissions(0o644);

    for entry in WalkDir::new(source_dir)
        .into_iter()
        .filter_map(|e| e.ok())
    {
        let path = entry.path();

        if path.is_dir() {
            continue;
        }

        let relative = path
            .strip_prefix(source_dir)
            .with_context(|| {
                format!(
                    "Unable to calculate relative path for {}",
                    path.display()
                )
            })?;

        let archive_path = normalize_archive_path(relative);

        println!("  + {}", archive_path);

        zip.start_file(&archive_path, options)?;

        let mut input = File::open(path)
            .with_context(|| format!("Unable to open {}", path.display()))?;

        io::copy(&mut input, &mut zip)
            .with_context(|| format!("Unable to add {}", path.display()))?;
    }

    zip.finish()
        .context("Failed to finalize deployment ZIP")?;

    Ok(())
}

fn normalize_archive_path(path: &Path) -> String {
    path.components()
        .map(|component| component.as_os_str().to_string_lossy())
        .collect::<Vec<_>>()
        .join("/")
}
