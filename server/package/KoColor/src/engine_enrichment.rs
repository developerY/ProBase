use crate::CielabData;
use std::f64::consts::PI;

/// Maps chemistry base to thermodynamic phase.
pub fn get_chemistry_phase(base: &str) -> String {
    match base.to_uppercase().as_str() {
        "WATER" => "HYDROPHILIC_AQUEOUS".to_string(),
        "SILICONE" => "HYDROPHOBIC_SILOXANE".to_string(),
        "OIL" | "WAX" => "LIPOPHILIC_LIPID".to_string(),
        "ALCOHOL" => "VOLATILE_SOLVENT".to_string(),
        _ => "UNKNOWN_PHASE".to_string(),
    }
}

/// Converts a hex color string to CIELAB coordinates.
/// Uses D65 reference white.
pub fn hex_to_cielab(hex: &str) -> Option<CielabData> {
    let hex = hex.trim_start_matches('#');
    if hex.len() != 6 {
        return None;
    }

    let r = u8::from_str_radix(&hex[0..2], 16).ok()? as f64 / 255.0;
    let g = u8::from_str_radix(&hex[2..4], 16).ok()? as f64 / 255.0;
    let b = u8::from_str_radix(&hex[4..6], 16).ok()? as f64 / 255.0;

    // 1. sRGB to Linear RGB
    let to_linear = |c: f64| {
        if c <= 0.04045 {
            c / 12.92
        } else {
            ((c + 0.055) / 1.055).powf(2.4)
        }
    };

    let rl = to_linear(r);
    let gl = to_linear(g);
    let bl = to_linear(b);

    // 2. Linear RGB to XYZ (D65)
    let x = rl * 0.4124 + gl * 0.3576 + bl * 0.1805;
    let y = rl * 0.2126 + gl * 0.7152 + bl * 0.0722;
    let z = rl * 0.0193 + gl * 0.1192 + bl * 0.9505;

    // 3. XYZ to CIELAB (D65 Ref White)
    let xn = 0.95047;
    let yn = 1.00000;
    let zn = 1.08883;

    let f = |t: f64| {
        if t > 0.008856 {
            t.powf(1.0 / 3.0)
        } else {
            (7.787 * t) + (16.0 / 116.0)
        }
    };

    let fx = f(x / xn);
    let fy = f(y / yn);
    let fz = f(z / zn);

    let l_star = 116.0 * fy - 16.0;
    let a_star = 500.0 * (fx - fy);
    let b_star = 200.0 * (fy - fz);

    // 4. Hue Angle (hab)
    let mut h_ab = b_star.atan2(a_star) * (180.0 / PI);
    if h_ab < 0.0 {
        h_ab += 360.0;
    }

    Some(CielabData {
        l: l_star,
        a: a_star,
        b: b_star,
        hue_angle_hab: h_ab,
    })
}
