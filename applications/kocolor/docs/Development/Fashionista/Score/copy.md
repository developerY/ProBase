The FASHIONISTA engine replaces subjective fashion advice with a deterministic, reference-calibrated mathematical instrument. By decomposing an outfit into foundational geometric, chromatic, and topological vectors, we map aesthetic harmony to a rigorous computational standard rather than relying on black-box AI recommendations.

**The Calibration Curve**
The final score is a calibrated logistic projection constrained strictly between 0 and 100:


$$F = 100 \cdot \left( \frac{1}{1 + e^{-\frac{Q-\mu}{\tau}}} \right)$$


Here, $\mu$ (calibration center) and $\tau$ (scale) are frozen, versioned parameters derived offline from an expert-rated reference corpus, ensuring absolute replicability across identical inputs.

**The Bounded Nonlinear Interaction Model ($Q$)**
FASHIONISTA isolates foundational evidence ($Q_{base}$) from synergistic visual relationships ($Q_{interaction}$), blending them via a fixed contribution parameter ($\lambda$) and aggressively penalizing unresolved perceptual chaos ($P_{unresolved}$):


$$Q = \left[ (1 - \lambda)Q_{base} + \lambda Q_{interaction} \right] - P_{unresolved}$$

**Dynamic Evidence Normalization**
Every extracted feature $x_i$ is paired with an availability coefficient $a_i \in [0, 1]$. If an observation lacks specific data (e.g., a flat-lay photograph lacking facial biometrics), the engine dynamically scales the mathematical denominator. This isolates the aesthetic evaluation from the data completeness metric:


$$Q_{base} = \frac{\sum_i w_i x_i a_i}{\sum_i w_i a_i}$$


Missing data lowers overall measurement coverage ($\frac{\sum w_i a_i}{\sum w_i}$) but mathematically cannot artificially deflate the core aesthetic evaluation.

**6-Dimensional Feature Extraction**

* **Color Harmony:** Transforms device RGB to perceptually uniform CIELAB ($L^*C^*h^\circ$) space, evaluating pairwise harmonic distances via the precise CIEDE2000 equation ($\Delta E_{00}$) and circular hue variance.
* **Silhouette & Proportion:** Computes the Visual Center of Gravity across a spatial mass matrix, defining spatial equilibrium as $\bar{y} = \frac{\sum m_i y_i}{\sum m_i}$.
* **Texture Harmony:** Extracts Gray Level Co-occurrence Matrix (GLCM) matrices, contrasting Angular Second Moment (order) with localized entropy (complexity).
* **Composition:** Quantifies category adjacency tensors, semantic completeness, and layering density.
* **Visual Hierarchy:** Calculates focal point isolation through spatial saliency gradients, evaluating the shift from primary to tertiary focal zones.
* **Presentation Integration (Conditional):** When biometric data is available, grounds the palette mathematically via the Individual Typology Angle ($ITA = \left[\arctan\left(\frac{L^* - 50}{b^*}\right)\right] \times \frac{180}{\pi}$) and Michelson facial luminance contrast ($C_f = \frac{L_{skin} - L_{feature}}{L_{skin} + L_{feature}}$).