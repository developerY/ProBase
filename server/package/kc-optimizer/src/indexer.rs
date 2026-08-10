use crate::models::KpssSource;
use std::collections::HashMap;
// use walkdir::WalkDir;

/// Traverses raw_assets/, parses JSON, and validates that semantic identity
/// comes strictly from the KPSS payload, completely ignoring the directory path.
pub fn build_canonical_index(_raw_assets_dir: &str) -> HashMap<String, KpssSource> {
    let index = HashMap::new();

    // TODO: WalkDir logic to find .json files.
    // TODO: Parse into KpssSource and insert into index by id.
    // SAFETY CHECK: Throw compiler error if logic attempts to infer brand/category from path.

    index
}
