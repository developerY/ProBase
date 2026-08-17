# **Architectural Specification and Taxonomic Integration of Classic Products in the KoColor Ecosystem**

## **Computational Framework of the Glow Archive Taxonomy**

The structural organization of digital cosmetic databases requires a deterministic classification system capable of resolving physical product characteristics into computational attributes. Within the KoColor enterprise ecosystem, the Glow Archive Taxonomy operates as a three-tier hierarchical framework designed to align user interface presentation with algorithmic engine calculations. The architecture bridges client-side UI navigation in VanityLandingScreen.kt and data structure mapping within CosmeticItem.kt, establishing predictable data contracts across product discovery, inventory management, and algorithmic recommendation workflows.  
The taxonomy functions across three distinct operational abstraction levels:

* **Level 1: Macro Categories (The UI Layer)**: Spatial and functional buckets designed for body-zone mapping and top-level navigation within the application, encompassing Skincare & Prep, Complexion (Base), Color & Dimension, Eyes & Brows, Lips, and Tools & Hygiene.  
* **Level 2: Micro Categories (Product Type)**: Technical classifications that establish a searchable database of specific product formats, such as Primers, Foundations, Contours, Eyeshadows, and Lipsticks.  
* **Level 3: Professional Facets (The Engine Layer)**: Expert-status physical, chemical, and optical attributes used by the AI calculation engine to evaluate compatibility, filtering, and shade analysis, comprising Formulation, Chemistry, Finish, Coverage, and Temperature.

This multi-layered architecture ensures that high-level navigation remains intuitive for consumers while preserving granular technical metadata for downstream calculation modules.

| Taxonomy Layer | Structural Role | Primary Functional Target | System Consumers |
| :---- | :---- | :---- | :---- |
| **Level 1: Macro Categories** | Spatial & Functional Mapping | High-level UI routing, anatomical zone selection, inventory segmentation | VanityLandingScreen.kt, Navigation Graph |
| **Level 2: Micro Categories** | Format Classification | Entity resolution, search index filtering, product categorization | Catalog Search API, Inventory Service |
| **Level 3: Professional Facets** | Engine Metadata Layer | Compatibility matrix processing, pilling prevention, undertone matching | CosmeticItem.kt, Layering Compatibility Engine |

The integration of Level 3 Professional Facets into the computational pipeline resolves key technical challenges inherent to digital beauty platforms. By isolating Formulation, Chemistry, Finish, Coverage, and Temperature into structured fields, the engine performs real-time validation of layered products. For instance, chemistry classification prevents formulation pilling by analyzing phase boundary interactions between hydrophobic and hydrophilic film formers, while temperature attributes prevent chromatic dissonance during automated shade matching.

### **Architectural Mapping in CosmeticItem.kt**

The data model underlying the Glow Archive relies on strongly typed enumerations to prevent data corruption and eliminate runtime string-parsing overhead. Within the CosmeticItem.kt implementation, each entity encapsulates its taxonomy mapping alongside physical performance metrics. The core entity structure binds a unique item identifier and display name directly to a macro category enumeration, a micro category enumeration, and a full set of professional facets.  
The facet set requires explicit values for formulation type (Liquid, Cream, Powder, Gel, Balm), chemistry base (Water, Silicone, Oil), visual finish (Matte, Satin, Radiant, Metallic, Glitter), coverage level (Sheer, Light, Medium, Full, Buildable), and temperature bias (Warm, Cool, Neutral, Olive). This strict typed system guarantees that any product registered in the system exposes all five Professional Facets. This completeness allows the algorithmic engine to evaluate cross-product interactions deterministically without executing fallback null-handling routines during complex calculations.

## **Classic Product Specifications and Taxonomy Mappings**

To establish baseline standards for the KoColor catalog, three classic cosmetic products have been fully specified according to the Glow Archive Taxonomy. These products represent foundational benchmarks across distinct anatomical zones, chemical formulations, and temperature profiles:

* **KoColor Signature Crimson Lip Color**: A classic neutral red lipstick categorized under Lips, formulated on an oil base with a satin finish and full coverage.  
* **KoColor Velvet Canvas Primer**: A foundational smoothing primer categorized under Skincare & Prep, formulated as a silicone gel with a matte finish and sheer coverage.  
* **KoColor Sculpt & Define Contour Powder**: A cool-toned contouring powder categorized under Color & Dimension, formulated with a silicone-bound powder matrix, matte finish, and buildable coverage.

### **KoColor Signature Crimson Lip Color**

The KoColor Signature Crimson Lip Color represents the quintessential classic red lipstick. Engineered with a balanced 1:1 ratio of blue-spectrum to yellow-spectrum pigments suspended in a lipid-rich matrix, this product provides an anchor for lip catalog visual rendering and recommendation benchmarking.

| Taxonomy Hierarchy | Attribute Field | Assigned System Value | Architectural & Physical Rationale |
| :---- | :---- | :---- | :---- |
| **Level 1: Macro** | Category | Lips | Restricts UI rendering to lip zone maps and application overlays |
| **Level 2: Micro** | Category | Lipstick | Routes item to standard bullet/lip-dispenser inventory indexes |
| **Level 3: Facet** | Formulation | Cream | Establishes high plastic viscosity for smooth, non-tacky deposition |
| **Level 3: Facet** | Chemistry | Oil | Formulated on a lipid/wax phase (Ricinus Communis seed oil, Candelilla wax) |
| **Level 3: Facet** | Finish | Satin | Delivers an intermediate specular reflectance value (refractive index \~1.47) |
| **Level 3: Facet** | Coverage | Full | High mass concentration of inorganic/organic pigments (\>25% dry weight load) |
| **Level 3: Facet** | Temperature | Neutral | Balanced chromaticity coordinates eliminating warm or cool color bias |

The lipid-dominant oil chemistry of the Signature Crimson Lip Color mandates specific handling within the interaction matrix. Because oil-based cream formulations display high solubility with organic emollients, the layering engine flags potential bleeding if applied over un-set water-based liquid lip stains without an intervening hydrophobic wax liner barrier.

### **KoColor Velvet Canvas Primer**

As the foundational base layer in the Skincare & Prep macro category, the KoColor Velvet Canvas Primer creates a uniform micro-surface topography over the stratum corneum. It minimizes the depth of pores and fine lines while regulating sebum-induced surface shine.

| Taxonomy Hierarchy | Attribute Field | Assigned System Value | Architectural & Physical Rationale |
| :---- | :---- | :---- | :---- |
| **Level 1: Macro** | Category | Skincare & Prep | Positional mapping at Stage 0 of the face-application sequence pipeline |
| **Level 2: Micro** | Category | Primer | Assigns high priority for base layer skin surface adhesion evaluation |
| **Level 3: Facet** | Formulation | Gel | Cross-linked thixotropic elastomer network that liquefies under shear stress |
| **Level 3: Facet** | Chemistry | Silicone | Cyclopentasiloxane and Dimethicone Crosspolymer solvent matrix |
| **Level 3: Facet** | Finish | Matte | Spherical silica micro-beads scatter incident light across ![][image1] |
| **Level 3: Facet** | Coverage | Sheer | Zero optical pigment suspension; high luminous transmittance (\>98%) |
| **Level 3: Facet** | Temperature | Neutral | Translucent optical matrix that imparts zero undertone modification to the skin |

The silicone gel matrix serves as a continuous physical film that optimizes the surface energy of the skin for subsequent base cosmetics. Within CosmeticItem.kt, its silicone chemistry designation acts as an anchor point, dictating that subsequent complexion layers achieve maximum chemical stability when formulated with complementary silicone volatile carriers.

### **KoColor Sculpt & Define Contour Powder**

Designed to emulate natural structural shadow within the Color & Dimension macro category, the Sculpt & Define Contour Powder utilizes iron oxide and ultramarine pigment dispersions bound by silicone-treated micro-powders.

| Taxonomy Hierarchy | Attribute Field | Assigned System Value | Architectural & Physical Rationale |
| :---- | :---- | :---- | :---- |
| **Level 1: Macro** | Category | Color & Dimension | Maps to high-relief facial anatomy zones (zygomatic arch, temporal region) |
| **Level 2: Micro** | Category | Contour | Specialized depth-creation algorithm classification; distinct from thermal bronzers |
| **Level 3: Facet** | Formulation | Powder | Micronized talc, mica, and synthetic fluorphlogopite particle matrix |
| **Level 3: Facet** | Chemistry | Silicone | Coated pigment particles bound with dimethicone for hydrophobic skin adhesion |
| **Level 3: Facet** | Finish | Matte | Zero specular reflection; prevents light highlights from collapsing visual depth |
| **Level 3: Facet** | Coverage | Buildable | Variable particle deposition capability dependent on brush application density |
| **Level 3: Facet** | Temperature | Cool | Elevated blue/grey pigment ratio (![][image2]) simulating realistic ambient light extinction |

The cool temperature classification distinguishes this contour powder from warm bronzing products. The recommendation engine utilizes this distinction to prevent improper shade assignment during automated facial sculpting routines, ensuring that shadow-creation steps maintain true absorption spectrum properties rather than adding warm thermal radiance.

## **Interfacial Chemistry and Layering Compatibility Engine**

The success of multi-product cosmetic application depends upon the thermodynamic and chemical compatibility of adjacent product layers. Incompatibility between underlying base formulations leads to film fracture, phase separation, and the formation of visible particulate aggregations, commonly known as cosmetic pilling.  
Cosmetic bases fall into three primary chemical phases:

* **Water-Base Phase**: Formulated with hydrophilic film formers such as polyvinylpyrrolidone (PVP), acrylates, or sodium hyaluronate.  
* **Silicone-Base Phase**: Formulated with hydrophobic siloxane polymers, including cyclopentasiloxane, dimethicone, and dimethicone crosspolymers.  
* **Oil-Base Phase**: Formulated with lipid carriers, vegetable oils, synthetic triglycerides, or mineral hydrocarbons.

Pilling occurs primarily when the interfacial shear force exerted during application exceeds the cohesive strength of the drying base film, or when a topically applied solvent solubilizes the underlying polymer matrix unevenly. When a water-based product containing hydrophilic film formers is applied directly over an un-evaporated silicone or oil film, the mismatched surface energies prevent wetting. The top layer beads up due to high interfacial tension (often exceeding ![][image3]), stripping the underlying film from the stratum corneum and rolling it into discrete debris particles.

| Base Chemistry Transition | Compatibility Rating | Interface Thermodynamic Behavior | Algorithmic Risk Mitigation Path |
| :---- | :---- | :---- | :---- |
| **Silicone over Silicone** | Optimal | Miscible polymer networks; seamless cross-linking between silicone carriers | Allow standard flash-off duration (30 seconds); proceed to next step |
| **Water over Silicone** | High Risk | Surface tension mismatch (![][image4]); liquid dewetting causes film separation | Require alcohol/volatile carrier flash-off or introduce an amphiphilic binder |
| **Oil over Water** | Moderate Risk | Emollients penetrate drying aqueous polymers, causing swelling and film softening | Force absolute drying interval (120 seconds) in application pipeline |
| **Silicone over Oil** | Poor | Dissolution of lipid matrix; surface slippage without stable mechanical anchoring | Restructure layer order: apply lower viscosity/higher volatile layer first |
| **Water over Water** | High | Hydrogen bonding between hydrophilic phases; uniform film coalescence | Verify total polymer load to prevent over-saturation gelation |
| **Oil over Oil** | High | Lipophilic phase fusion; homogeneous blending of emollient layers | Monitor pigment displacement risk due to excessive plasticization |

The compatibility engine encoded within CosmeticItem.kt processes these chemical phase attributes dynamically. When a user creates a vanity routine containing multi-layered products, the system executes an interfacial evaluation pass. The engine extracts the chemistry enum of the base layer (![][image5]) and compares it against the subsequent layer (![][image6]). If the pair exhibits high thermodynamic risk, such as Water over Silicone or Silicone over Oil, the pipeline triggers an automated alert, suggesting an interstitial drying phase, an amphiphilic balancing primer, or an alternative product pairing.

## **Colorimetry, Undertone Mechanics, and Temperature Facets**

Color classification in the Glow Archive Taxonomy transcends simple visual nomenclature by anchoring shades to established colorimetric models. The Temperature facet in Level 3 divides products into Warm, Cool, Neutral, and Olive categories based on their spectral reflectance distributions and coordinates within the CIELAB (![][image7]) color space:

* ![][image8] **Axis**: Defines Luminance and Opacity, ranging from 0 (Pure Black) to 100 (Pure White).  
* ![][image9] **Axis**: Represents the Red/Green Chromatic Position (positive values indicate red, negative values indicate green).  
* ![][image10] **Axis**: Represents the Yellow/Blue Chromatic Position (positive values indicate yellow, negative values indicate blue).

The Temperature classification is calculated directly from the ratio of ![][image9] to ![][image10] coordinates measured via spectrophotometry under standardized CIE Standard Illuminant D65 lighting conditions.

| Temperature Facet | CIELAB Vector Characteristics | Spectral Absorption Profile | Skin Undertone Synergies |
| :---- | :---- | :---- | :---- |
| **Warm** | **![][image11]** (Red), ![][image12] (High Yellow) | Peak reflectance in ![][image13] range; strong blue absorption | Golden, Peachy, Warm Yellow undertones |
| **Cool** | **![][image11]** (Red), ![][image14] or Low ![][image12] (Blue/Pink) | Enhanced blue scattering below ![][image15]; reduced long-wave yellow | Pink, Red, Blue-Red, Rosy undertones |
| **Neutral** | **![][image11]** (Balanced Red), ![][image16] | Flat spectral distribution across visible spectrum (![][image17]) | Balanced undertones; universally compatible |
| **Olive** | Shifted ![][image11] (Muted Red), ![][image12] with Green shift | Distinct green spectrum reflectance peak near ![][image18] | Green, Muted Neutral-Cool, Golden-Olive undertones |

The formulation science behind the KoColor Signature Crimson Lip Color demonstrates the precise colorimetric balance required for a true Neutral classification. To achieve a shade that exhibits neither cool blue-pink nor warm orange-yellow bias under varying light sources, the pigment matrix combines organic and inorganic colorants in calibrated proportions:

* **D\&C Red No. 6 / No. 7 (CI 15850\)**: Serves as the primary red chromophore providing high ![][image11] intensity.  
* **FD\&C Yellow No. 5 (CI 19140\)**: Acts as a warm counter-balance to shift the ![][image12] coordinate upward.  
* **D\&C Red No. 27 / No. 28 (CI 45410\)**: Functions as a cool blue-tone adjuster to depress excess yellow undertones.  
* **Iron Oxide Red (CI 77491\)**: Operates as a muted spectral attenuator for ![][image8] luminance dampening.

Mathematically, the neutral chromatic equilibrium is maintained when the calculated color angle hue (![][image19]) satisfies:  
![][image20]  
If ![][image19] exceeds ![][image21], the color shifts Warm; if ![][image19] falls below ![][image22], it shifts Cool. By locking the KoColor Signature Crimson Lip Color to ![][image23], the system guarantees that the product retains its classic red appearance across diverse user skin complexions without clashing against underlying natural skin undertones.

## **System Implementation in VanityLandingScreen.kt and Technical Synthesis**

The presentation layer implemented in VanityLandingScreen.kt provides user interaction pathways that directly query the taxonomy schema. To maintain dynamic performance during multi-facet filtering, the UI architecture maps user gestures directly to indexed properties within the dataset.  
The interface employs a three-tier progressive disclosure pattern:

> 1. **Macro Navigation Bar**: High-level icons representing Level 1 categories (e.g., Lips, Complexion) partition the full item space into targeted collections. Selecting a macro tab updates the active layout state and reduces the searchable dataset size.  
> 2. **Micro Sub-Category Carousel**: Horizontally scrolling chip groups allow users to isolate specific product types (e.g., refining Lips to Lipstick or Stain). This secondary filter narrows the item list to functional equivalents.  
> 3. **Professional Facet Control Drawer**: An expandable filter drawer exposes Level 3 parameters (Formulation, Chemistry, Finish, Coverage, Temperature) as multi-select attribute chips.

| UI Component File | Responsible Taxonomy Level | Rendered UI Element Type | Reactive State Binding |
| :---- | :---- | :---- | :---- |
| VanityLandingScreen.kt | Level 1: Macro | Primary Horizontal Tab Row | selectedMacroCategoryState |
| MicroCategoryFilterBar.kt | Level 2: Micro | Filter Chip Carousel | activeMicroCategorySet |
| FacetFilterDrawer.kt | Level 3: Professional Facets | Multi-Section Expandable Accordion | activeFacetMap\<FacetType, Value\> |
| CosmeticGridAdapter.kt | All Levels | Composite Product Cards | filteredCosmeticItemList |

When a user adjusts a Level 3 facet—such as filtering for products with a Silicone chemistry base and a Matte finish—the UI framework executes a predicate evaluation across the active catalog list. The filtering pipeline evaluates each candidate item against the selected Macro Category, Micro Category, and all key-value entries in the active Facet Map. An item is retained in the view model list only if it matches the higher-level categories and satisfies every non-empty facet criteria specified by the user.

### **Strategic Enterprise Benefits**

The implementation of the Glow Archive Taxonomy establishes an enterprise platform for digital beauty applications, product inventory management, and computational shade mapping. By unifying UI filtering mechanisms with low-level chemical and colorimetric metadata, KoColor provides a scalable infrastructure capable of supporting advanced virtual try-on algorithms, automated regimen composition, and precise formula compatibility checks.  
By tracking the Chemistry facet across all catalog entries, the application proactively prevents phase separation and cosmetic pilling before product purchase or application. Utilizing CIELAB spectrophotometric boundaries for Temperature facets ensures that classic items, such as the KoColor Signature Crimson Lip Color, perform predictably across diverse user skin tones. Furthermore, the modular structure of Level 3 Professional Facets permits the introduction of future technical parameters, such as Viscosity Index or Volatilization Half-Life, without breaking existing database schemas or client-side UI components. This structured taxonomy provides the digital foundation required to bridge physical cosmetic chemistry with intelligent software systems, positioning KoColor as an advanced, highly reliable platform for professional makeup artists and consumer users alike.

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACcAAAAZCAYAAACy0zfoAAABuElEQVR4Xu2VSytGURSGl0uMlBQTEjJQChmYMFBIjMSf4C9Qyg8wIEbSNxEDBgYSMlLCSPwDkZLLxGXgvl57rVpnncN3mZicp97Oft+1z/r2dy77EKWk/B/trHvWF+uYVRktR5hifYgGXQ00s04o9Np3NVBB4dxd1qmrxRhnzRu/QqFxp8mUN9aSjMtZn3JUeimcq3Q4D86d33Y+Ak72DZIyXNlb4+cozOkyGfyE8eCVwt1QhswY3Dgf4YriC/GLaxRfbzLQZsY1FObgaNmTXDk04yT/J5MUmg2b7EgyUMYaMDVlmuJ/EmQomlezHlkLrAuTZ2WEQiPcMoteyTUKz1WreDtvUzLPIiXneTHLWqfwJvW5mi5uxmRNktWJPxDv0Wez1hcKAU3QbMtkujgPsmsZr4r34PYhL/WFQvGL8V6x+W/P3DIl5zmB26h7l6I/2iNeN1WPXVy3jLO9rTkzRslXRbMS8S3iPcgyzo8aD55YDy7LGTS0uzz2LmR+535h7RiPz5hfMD5J78YXUZjTYLK8qKLwGUKTOzni9U/ikkIduz7OKY6WfzhjPbM2KMztj5ZTUlJSUsA3TUaCwtybN34AAAAASUVORK5CYII=>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADcAAAAaCAYAAAAT6cSuAAABv0lEQVR4Xu2WzSsFURjGH1+xYEn5yEfYUfZKFmysUNYWSvkDLIkVW2FHirKysFHorqyIjbK1YIFSskA+4329M9fMO2fkzL2Xq86vnm7nec6Zec+5c84M4HDkPU2kQm3+d85I755KVebTQJrW5h/QQjqA1JpSWSxrkAGaIohfRhogXZOGQj1+j26Ea+xQ7Vi405M2A/BqPWgzS7RpIwaucUx5z6R95UXggabHrgSSVZL6SY+k3lCP5AxDrj2iAwNVkL78G2TX82NpxtejN0laIRWEegD1pAnlJWUKcr8+5X8H12WaBNdq8tOsQjrcQg6Udq+tJ5gpS6Q3yF6xZRPmSSzC7Kfh8MXg8QWzwRbpnlSnAwv2YJ7EHMSv1YEPhzMG70p5NvC/fkS6JFWoLAnrME9uAeIX64CphoTlAY8LY49XJSl8s1PSCeR1kilxe24ZZv+TUUTDcc9rVX4SeKEOSRcIL6AtnZCarE7LLkRDbp8rLxvwHr4j1ejgh3Bdg8rj690oL0RwcrOqnQvmSa+QU9mGHcg4H3/7NAa8CPzBzJ1Y2yrLJf7jb/NRcAw5eTcgY3vCcf5hMzmHw+FwOBzEB/yQZa1DkIHLAAAAAElFTkSuQmCC>

[image3]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFYAAAAZCAYAAACrWNlOAAADPUlEQVR4Xu2YS6hNURjHPySUV1LKo3uLQkIhkscVosgjSkYSBtfAgJAIE48iA8IAKTHCUAYMZKBkgkKKdMv7nWfk+f3vWp/7nf9e+1y3c85N2b/6d8/6rcdeZ5191l7nihQUFPz7DGZRUDkjNatZ1pLRmteaX5qrmt6l1X/Yqnmv+axZSXXVZrbmjYQ5WZhPUlr/rrQ6w1MWtaRRc8CVT0mY5BjnwB3NRVe+pbniyrUCd9glCXO6TXVGatFT4KZoN1J3A7ueVDbg8u7uaoGFHa/5JuF6k0urm0nNjVmhmcGyljyS7MR4YW9Q2YA7xrLK2MJ2luy8jJRjfrJobzZLmOgc58q9oZQ3lmt2ac7G8gjNUc08a6AM1RzR7HTOYwsLNkm43sOW6mbKzcG4SeVFEsY7HstdNGs12yV8iMZGCW2GONdmFkqY5H7yeQuY540d0tKmSdND0yGWsTiXNQ2xLV6nxvILC+yBtcC5VD8PniH9ya3XPJbQF4u8Jvrd0dVJ2NvBsOjmx3Kb2Kc5o/kh2b0obwHzvMe2mq7OrYsO+54HDicUDxZ2givbB+Ov29oc8upxF6LuNHk43jrg3pJrEwMkDHLOOX4jRp73NEm2DRaLHYCbRg5tJ5LDUQ9tcRwDqbGMXpqTLCP1EvrWkYc7nHDlrvNX8CBcNvK8575k26xKOAA3nRwWdhI5YOfuvLEMbDGdWEYGSujbjzzcnoQrd50M+OrjgeKxQexo8yGWGbi7LIl7ku1rdxyTt7BTyBk2z9RYRrk6+3b2IV/xwi6WdAdz9kkviWUGbixLokmyffPuMjje37GwU8kZcyU9f2OcZgNLR72Evn3JV7ywAI1x1DBGRXfeOQCHX2nG3uha46Vk222Jzh9ruke31LmOmheaBxIeWilQx+Mbz1kQuCnQdzh5uBMJl3edJPga4AmITq/i30MlLQLdJNRdk3Am/CL5b9bAPohTAY5WeJMzJWwrOObA4bf7BQnbCV7DoQ7/i8DRBn3gMAY+IHx1U+BXWYqPLByY/xMJ4+PvV81BzbPocE3872GQhA8XZQRr9F+zTDOLZUHlfGdRUB2usyionG2SPZsWVIEGFgUFBQXtzG/PGgK0MVhTLgAAAABJRU5ErkJggg==>

[image4]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGsAAAAZCAYAAAA2VdDGAAADrklEQVR4Xu2YWaiNURiGPzMZcoFISVKShAtuTCeUGykyZMoFN6YkSVxIkgtSFAlJKS4kxIX5BhemFBlu6BzzPM+z7+1bK2u/Z+39/2efvdlH/1Nv56x3rX/tb69vTf8WycjIyKhUOrGRUbn8YqMhslfsi3xRLaU6T0/VebF2J6muHDxXfRb7PGh4brWsdH6oQjRRHWazoYEv2ZbKX4MyqHK+pz+Vy0mYsBiYOGm2tz2qVmyWk0Fs1JNhYoNwKvBeOq9X4KE8JygDJPQceeXgo2qhWAyPqA4gCY3YjJAv2WWjuapGdVbSBZhEU7EvsS7wMDjw2rkyZi3KPHuPO7/cIB7wTuzzJgV1AMlKoptqLZt/i8aqK2KJK/XS5i1nBZU9OyXue4aqFqu2qtqLxTxLtUH+TAQwV2zAqwIvxCcLcGwgTbJuUrmD2E6BWPyOMVq1W9XHN1LGiZ3nWNkl4YjqraozV9SRZqrrqp+Su2oPSu0BApsl7numqy6LtRmh2ub8sc5DvNXO6+i89a4cEiZrmli7N4GXJlk/qNxDtV2sL8R11/lIIrwlqm+q1mKTDN5r16YkYKZ/V/XlihRMVW1RPVEdorrTEk/KRjG/K1cQaHMn4nGfuImyBz5R+alYu/munJSsMaoZbDpicWDiw8OR41nmvJKzRqxjXB6K4bHkBobBiAW6SczHuVcItJkZ8W6Qh9US+xwkkfGDjFmflCxOdgj6qCYPE5bjWBTxSsI8sY6ncEVKcM7geX/zyndm7ZC4z6DN5Ih3gTy8V8X6w3bE4HxBW1zrk5L1kI0A9MHn2X3nh2AVs1cvVot1OJIrCrBLbOsMGSLWjw9usPu/2Nsg2oyPeJwsv70xfN54bou1j608z3JVPzYD8PxV8u45PwSXIPaKAjMcs683V6TAJ6Uq8GY7L5yRsQF/L/ZOlgSenRDxOFnPnM/gwpMPH38+CtUB1F8jL7ay/G5VNEdVr8RuUsVyQHWGPCQBgbUIvGOSuwJxW0Sb7oEXo6VYO36hhneLvA/OD1ngvHw33QFS+xkPztITbBJ4FispBLc+7nOV8/CTVWowSJfEtgAMRCnYLxZIjfuLAzm8CXnwXocB3SfWblRudS2wcnDuYTAeiD07UewAh4cZ7FcmziuU4WM7HCg2aN5DP3y2ePL9TomzrA2bDrw+hHG8cD7+hnHgSMDLOOL3cfBtOS+l/rnpf4ZXR0aF0kX+4c9LGXXjIhsZlUvs3SyjQkn6CSwjIyMjo3z8Bs0+FM2pwl5bAAAAAElFTkSuQmCC>

[image5]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAAaCAYAAABVX2cEAAAA30lEQVR4XmNgGAWUgvlA/BmI/0PxAhRZCPjLgJAHYWdUaUyArBgb2AfEKuiC2AAjEG8H4vUMEMOCUKXBAJclGCAfiE2gbFyu+4MugAu8RWJ/YIAYxockpgbEnUh8vADZJaBwAfFvIoktA2IeJD5OAAqvzWhi6F7F5m2sADm8YCCPAWJAN5T/C0kOL3iHLgAFMNdpA3ELmhxOgMsLuxkgcveBmBNNDitgBeK96IJQwMSAGXY4ATMQvwHik+gSSOAbEP9AF0QHq4D4IwMkfYHSFSjvYQP6QJyNLjgKRsGQBgDXhTTgumYxJQAAAABJRU5ErkJggg==>

[image6]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADYAAAAaCAYAAAD8K6+QAAABUUlEQVR4Xu2WsUoEQQyGo2IhyKGFbyA2Fja+wfUWvoagL2EjPoRWFjYWFleIdoKC1iIIgoKViiKIiKKJmcVsmFkvszpekQ8+2Plzm5twN+wCOI5Tkg30Cf0IbtaqzDt818luvVyESeDvNiM3HuMAndbhHzOFXsHPe0syhPbQHeCbF+vlL8xNBYc6yCBrsBV0PlynGrzpwMCRDjJI7auRO3H9ANygI7IZdE2srZzoIIOsweQNdI5ofS6yLXRcrK2c6iAD82B0vnZVppuYGkb4l8Hk+apYBm6yHtavotbEGHAv7Vkkq+wX82D3OghUjWbRVVVLMYEuRLyIZJX9Yh4s9eE94Nol8C/RhuJ/xVF0X4eBYTA2a6DoYCPoLXqsC4Jn9EWHGRQbbBt9BH5+0XOL3gVjzKFLOsygzWC0vxv0OkjXlA0EbQYbaOi8Oo7jOM5v8AlsIWKa0gxOVAAAAABJRU5ErkJggg==>

[image7]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADoAAAAaCAYAAADmF08eAAAB+0lEQVR4Xu2WyysGURjGXymyFGVDkiTZiYUtG5eFyz9gIQspt7W9HRsLKST/gA2KzWenpNgKfWXlknLL/fI+zkzfmdc5Z2Y+7M6vnvrmec77znxzzpkZIo/H84+UsRqk+RuWWLesT003rBl9kMYyq1CaeYDzmOhmPZPKM9Eogq0+lvBP2phgZVmTrFJyj3VxzZojdXN7yN4Hfrs0mQ/WEKl8ivUSjd0UkCrcl4GBN9aGNFOCZYnzYfZMDJL9BoAmUnmtDOIYIVXYJQMN3EUs6VFWDbkvxMUJa53UzI6ReUbOyN7/njVNKl9kXUVjN+dkbyzBnsYKcNHCWmA1y0DDdT5kO6w61hqrMRp/46q3gqK8Cg08sVaC39iLj2TuXSwNjfB6Oii3rTCLOq56K2i0K808wLJ6FR56bwrPxQCpmgrNwyoy3axUxO3PNmlYCJ+g9cKH1yo8F8f0809hGUsvNZfkboIHUBLC5aYTzk4aTKvL1Ds1riZ4TyWdUVOfU4MXB8b3Grys8FKBLxw0OZQBU0XpLhJjtw1eRvudBIzT9+dw4P2KWVJN+oQ/H/gHwnexRdELOgqO8a6sZo1rmYt3UjWgiFSPzlycjlVSL2o0RSNd+MTC1w9eC5VhQUL2SPV4YJWw+oPjC31QAu5I1eEpXi4yj8fj8Xg8nr/jC9w8jWfFmPHyAAAAAElFTkSuQmCC>

[image8]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABYAAAAaCAYAAACzdqxAAAAAxElEQVR4XmNgGAX0BvOA+BMQ/0fCH4G4D1kREpgPxMzogvgAzFBcoBCIHwBxERALMuBXCweMDBCFZ9ElsIA/QLwNXRAXyGaAGOyFLoEEUhkgQZQHxIoMRLr4JQORChkgcQLyIVGAUPiSDUCGnkAXpBQQCl8ndAFiwWsG/MEAijCyAL7wrWEg08WgHAQy9CK6BBDIMuC2kCDoZ4BoDkQTnwEVv4AmThAsBuJfQPyXAREUMPyPAZK7vgOxDEzDKBgFo4BCAAC9ITJIPxt2nwAAAABJRU5ErkJggg==>

[image9]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAAaCAYAAABVX2cEAAAAuUlEQVR4XmNgGAX0APFA7IguSCowBuKvQGwExCJA/BuIpVBUkAFeAfE9dEFSgR4Q/2OAuBDkov9ALIyiAg0wA3ElEOejSyCBOCC2RRdEB+0MkHAAAR4g/sUAsZ1kEM0A0ciBJHYWKkYyAGl6jkXsC5oYQRDCANGYjiYOEgOFH0lgBwOmd+SgYixo4gTBFAZMw5YiiYHYRANuBlTD3KB8mBi6RQSBMwPCgGyoGCiBgvhCMEWjYBQMOwAAFjYmeZHz6j0AAAAASUVORK5CYII=>

[image10]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAAaCAYAAABRqrc5AAAA00lEQVR4XmNgGNZAEYiZ0AWJBQ+B+D8Us6PJwYA8EDeiC6KDxQwQQ9ABMwNEnAOIA4H4NRCHoqhAAiCFP9EFkcBJIP6GLogOQIZgcy4rA0ROFIgDgPgHELuiqIACZQaEk+uAeD4QM6KoYGCQA+JaNDEUsIgBYsgHBkjA6kL56AbhBSANv7GIbUATwwtAGtqxiL1AE8MJJBkgGniQxEDeAIlNRBLDC9IYMNNHKVRMFU0cJ7BjwDQExH+EJkYQIBvSgcYnGoAyHkgjCO9AkxsFo2DwAAAHfiuwi61bFAAAAABJRU5ErkJggg==>

[image11]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACMAAAAaCAYAAAA9rOU8AAAA+0lEQVR4Xu2UwQpBQRSGT6EsWNthY2Ntr6xl6xE8gQUrjyAPICVbFy/gAexkY0OewYIonNPcW3PPnXLcuYWar76a+efW/HemBsDh+G2OPPgGS3SNrtAq+gwvvyeDdnloQQpUiQHLRWQhuTJTdI/O0QbEOJkcfFamjA7RJst1DjyQkgd5mRO68cdt9ALmv6erj4W0zBaiG9P8zDIxNYN1dGTIyYACqI07WkZQ1mOZmJZBOu6JIScDdhA9lZKfpVluheSaaFNeZmzIrJGWuRuyqzZOBEmZPoQ3pBeW5h6oR26hrVkhKUPMQBV4oEW04s9v+ke2SMs4HA7H3/ACoCA2dKPaHdcAAAAASUVORK5CYII=>

[image12]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACEAAAAaCAYAAAA5WTUBAAABEElEQVR4XmNgGAXDELACsTa6ID3BayD+D8W4wHwgZkYXpDYAOeAnuiAQFALxAyAuAmJBBvwOxQpAQVyKLogDgAxvRhdEAn+AeBu6IDGAg4E4RygzQBzBiS4BBKlA/BGI84BYkYGMkOBhIM4RixkQhoPYVUhyMDAPiBnRBYkBvAzEOQKWKDdB+e1A/BchTRkgxRFbsIgZoIkRBCZYsD0QT8IiDsIwIMsAsZAbSYwNKpaBJEYU8MOCw4F4ARZxEIaBmQyYiS0fKgbKkhQDYqLjGQOmI0C5AV2MbECMI/YB8XU0MZADQFmSKoAYRwQyoPoaVIQ/QOJTDIhxBAjUMSCyaSWaHMWAWEeMglEwCkYBzQEA2S44svOIYuIAAAAASUVORK5CYII=>

[image13]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGoAAAAZCAYAAADZl7v4AAAD9ElEQVR4Xu2YW4hWVRTHV5oi5YNEihDJoEJUBD0JUQ/ThTQVsTAvFDiBD2qBpEhFF6MUegkq8kEtRDTBCK8PUprQjTRIUexCIQPelS5qCpamrV9775l11uxv5juj4Aj7B39mr/9eZ39nn3325YxIoVAoFAp9m+dV61R3xfhO1Zroe15XXVb9q3qlWtXBaNUuCXnbXV0duP4T1WzV06oZqumqaVGWDyTkH1SNcHWJB1U/S8j7yNVdFyyWcPNWeyoZgc9VD5n4ZdVpE0OrhOsT97q4Dv6erL4zeZdUj5qY+nEmhvkS8hIMfm/v65qxSPWeapWEWdK/Wv0//VQnvCmhs7e5eI6J4R/VTuc1Aw/2PgkznVk6Mso+4DddDBMzHjErhffecl6fhsFhWeiOydK184DXEsvDYsxfy2fRrwMvxtveVP5Q3Wxi2v3RxAn822O50b3/LXm/z8IS1tNA8XDoFBoVPd5029HXXJxYKXm/Li+onnMe7eb2QXxWCdgRY0+75P0E+yAv8eoYD5Gw+vhZeKuEVeSd+BfGStgH705JyuOqj1XzjFeLl1RLJNx0eqjLKhmBDdI5WLtVZ6vVsjHWeZZK3q/DAMm3gbfZmxL8rbF8KsaeHyTvJ15UnZeQw+A8HP1t0UuwHK+I3nIJBxpgAPEWqi5IeNlZKfC4p9qw0X7qPBpj/ffsl87BQveYui+j53lXgm/3srocUD3jvNTp9c4H/F9NOXdfeyXvW2ZJyJnrfLwxGc+3dyZ6A43HxPB5vSb3o8ygJ2P5F+nMGRy9tTH2vC/Bv1H1TROiI55cu4C/yZsS/K9i+WiMPeml6442yefg+ZMlXrvzOID56/ns8V5T3OANCd9JtjG+q742MTwgIYclBBrtUR9K3m8WfrvR9fh+NQB8liNotEcxS3O+he+3XA7eYxnvJ+cdjr6FfdZ7TcFFv2c82xjldIiwvCqdeffH8tU49Vn8vVjwG536+DgGDku565s59T0l+Ry88Rlvn/MORd/CMuq9puAiNjzv2cYoTzFxgjWcNzNB3hMmBpZMjtW9xd+LhUHydewd3iO+JeNtcZ6nTbq2BXgTMh7LqSU3o57NeE1xTjXUxK0SGrrDeFOj58GzGyXL0EUTs6yS02K8unQ3UDx86gYZjw38exPDcanuH8MlXMdpsjsWSNc+3hS9mcYDPGaQJXfifCN6uX8s9AhLX3ogiCOnh1MXdcwQBoMyR1APpykGn//TkfNItbo2tHHEmwbaJ4dDxZ+qb6vVHfwmYXPn2N6ojxb6ye/y8I+pvpDQN8p41NHPSRLaxWMGpW2Ev8T4JyXs6X9Jtc3cp0WhUCgUCoVCoVAoFK6Q/wBwHUW4lBxFXwAAAABJRU5ErkJggg==>

[image14]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACEAAAAaCAYAAAA5WTUBAAAA6ElEQVR4XmNgGAXDELACsTa6ID3BayD+D8W4wHwgZkYXpDYAOeAnuiAQFALxAyAuAmJBBvwOpRiADG9GF0QCf4B4G7ogNYEyA8QRnOgSQJAKxB+BOA+IFRloGBKLGRCGg9hVSHIwMA+IGdEFqQlgiXITlN8OxH8R0vQBIAdswSJmgCZGEAgDsQmRWB2qBwRkGSAWciOJsUHFMpDEiALyQOxHJLaF6gGBmQyYiS0fKgbKknQBzxgwHQHKDehiNAX7gPg6mhjIAaAsSTcQyIDqa1AR/gCJTzdQx4DIppVocqNgFIyCUTB8AADiwDJ6RoYmegAAAABJRU5ErkJggg==>

[image15]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEEAAAAZCAYAAABuKkPfAAAChElEQVR4Xu2WS6iNURTHFyGFUmSCXFFEYo7cMCXJa6A8J6YMPTLAXDHwykRGhAkRymPgUUpR8qgjhDxKHgMR69de21lnnX3uPWb31v7Vv7PXb+/vcfb3ffv7RCqVSqV/pmj+RGnsldT3W7Ortesf0zV3JY27GvoGDZx8aRKuaRa7eqfmi6uhV1q3nRfqQcEVzXdpP/GhmvfBAeMmhnqbq+Gn5k5wA5ZJmguaj9I+CSsKDnA91p5gNb8eJra07YAkn2hpEkaZI9PMzbI6syfUmZNS9pmFmh2aI5qxku66LZoT0j6hGzUHNGesnq05plmWBygzNEc1+53rirOaydYuTQKck+ZEPNB8a+2W89YXOSxln1kvaX+MYc3hD8Byc1Othn3mSEMzRjPE6leaG5pFNpZ2X8dtYZzmuqs7TQI8kuZJkDmu76a5yEFJ3q8dJRjzsuAuBvfa/Ejntpvb7Bzg5gZXJJ54p0ngyq+29lNpTsRoc6etjhyS5IfFjgBjNhRcXFQb5j0sxtEBrjfKCM/hzOBKk3BKczu4BZLGPba605rAs13yEcasLTi+OTzPzXu2Fhzg/Gu9yCXNrRA2JLSP2zjqvCB6dkvz4POtHRezbt8OjFlZcPeCe2bew0IaHXQ1CSXyJES3KjjgCrxwdemP8Bh9Dq5E6Ri4+8E1zHv6uhOWRNkNpUlYU3CAG+Hqy5pfrs4rd49zJVjkGBc/tHCsP54P5j18veKGO8dahVvnXL/wmnon6VVDaPursEnSTrmy/FHa411/5qGkr07e5YxZ2trdBrf7W0nHfCNpWxZgvlBxvA3ynfTJajz97PurpO1w7IfH74m18z5/sHGlUqlUKpXK//AXUdnPBpNm+/AAAAAASUVORK5CYII=>

[image16]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEwAAAAaCAYAAAAdQLrBAAACgUlEQVR4Xu2YS6hNURjHP68QRSFJuUnMKEYyMCBlQgzMDFBIBgYyMCAmHiPdgTJBGEhSyiMTypCBZECZeJQo8og8Inz/vrXuXvt/1t7nrnu2ttNdv/p39vp/+3xrr7XX6xyR/mWs6gmbmThnVKtVfziQqaf1DpsvNtT7hdY67IVY5dBEinkGVIfZbJkmO+wuG924IPEHGCfmT1JtVL1VbSrd0R6x500FOeaqBlUPVSfL4WrwxR9sBtxTfWWzZZroMHBKLNcYDtSBL8Sm3ASx2CzVBtV31ZrSHfVMVy1hM8JMNoZBEx2GHEtVJ1SvVIdK0QoWSDHtDqrOSmdvz1MdIK8bn8XyYhrj83o5XKIuxuClfVC9U72X7uvPNrEO4TaF3GajjvNiDfootugvduW6CrpxTTWbvD1iedeTf1O1jLwmWCRW34Ar/3blnUN3FCSdDpDkZ8S7Sl4KWPOqeCaW3+tiOdwImC3IvT3w1jkPG1lPIMnRiPeGvBSG88bQqH/FN7E2hFSdBJKYI5ZkauBhKsLDVtsLK6QYRU/FNpAq7rBRQTgyWSEov454X8hLZod0VrbPeQvJT2Gt6r4U66Cv59zQHQUzVHvZ7IHlYnXxWgVvP3nJrJTODkP5JXmp8JroOS6WH+vWKtVpV24SHFE4J14+PGxquN5aDqcRJj9G5ZGymw3iiOq5JJysA7D2YUfHcyJPDMQ2u2u/Afh2PXafIwY/un3CWxT7H3kQXGMkXwrKnsmqX2JtuuE8HExRxvIwqkCj/SaFNamJGTFqwOip+w2cITC6prCZifNJNY3NTJxHqvHu+nIYyHSCMxz+gdii2iX270WmBn8E8rpSDmcy/cpf8vCYXvVaWlsAAAAASUVORK5CYII=>

[image17]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGoAAAAZCAYAAADZl7v4AAADmklEQVR4Xu2YWchNURTHlzFDSUjKmDwYXpRS4oHInPBgiPKJB1MJCZmKFyWFeDDlwfAgZHiQIYVkipCppK/MZMqUKax/e23WXXfd77vn8PB9tX+1unv99j77nnP2PXvvc4kSiUQikahddOT4ZaWwnOM9x2eOaaYu0oXjEoU+Tpm6LOD4/RwzOCZzTOSYwDFeQrOdQvuHHB1MXWQAxz0K7faYuloJLsQbqDscJ1V+i+O8ykF/Kjy2p8mzEM/Di8uq3U+OwSpH/VCVg/kU2kUw+HnPq0ZwguMTFV9EM8cBuOYmn6ly8I3jonHlgBvbh6M7hae0s4Q+j9UmByMdh7yb49YYVytox3GY4xUVX+h1xwE4TDugteT41GDwvWOroi7HOiuZNxxNVY5+8aRb4NtLebTklq/k+xpPPGlvoJBbB7RfocqaneT7rCzimGMc+vXWQfiNUj4tuaWSfB/BOriMY5fkmDlWUvFT2IrCLLJePsEQCutgj9iIGcOxj2Oucpk5QH9/gXkH6pAqazaT77PQgPw+4I5YScEfk/I7yS23yfeRxRxfKLTB4AwUj3VaH4fpeJu4rRQ2NAADCLeQ4zuFmQAzBRzOKTMtKfzqInkH6qwqazZQ8G1tRQYecEw1Ll70QeMB/H1V9s7rBvleM51Cm1nGw/V2nO0PO2S4hsotEZcZe1DegdqryppNFHx9CrvE6gIXYvH6BfBYVy3w56T8VHILdq2e11SQ3wbO7izhKo17IV4zz3HVsoWjq3F5B6rUGrWDfF8uu6n08fDHraTgMR2BUmsUnlLPa/D+5rWBG+a4u8Y9Fq/BOmtdtWAexy9PR7z5KMcd3QdxFji8RIK+kv+PXZ8mno8HfKldH16OwVLJLeXs+iaR3wZuuONuGvdIvAbTqHW58G7MOMcBuF4mH6ty8JHCtjov3vlEMEi2DmuHdchbOO6ocZYKKu4LwI1wHKZTjfdEzXZcLkrdGDi80UfWitNgGvqh8joU2nRSLiulzgfg5qOukXJYwK+qHDynwvWjDYXjsJusigUU2unNQBNxU5QDcHiCNN6Oc5W4esaXzTUKF4QvQ6B8RdU3pvAF+PsGOyZsXTEQFtTh3w38T4f2gwqrM4M+nlipQP9og03FW44LhdV/wNqLxR3TPdpjW10VmAnwvbgXzzjOULg2lOFQh+scRaFfODxBr3GwfCKHf8nRj8ISovv0Xi0SiUQikUgkEolEIvGP/AaEXktcSUINFwAAAABJRU5ErkJggg==>

[image18]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGoAAAAZCAYAAADZl7v4AAAD8klEQVR4Xu2YW6hOWxTHh2vCg3RcQpKUOAovXlBbyK2ElEQdyYODcouI2LkU5cG9kLbrCXlwScctIqdIiAgP2uVObrnkejD+xpwZ39jjs9anXbaav/r3ffM/x5zfWnOtNeZYH1EikUgkEjWb6azdrM6h3Ym1I/iWo6wvrOes8aYv0oF1jiTuuOkrhf2s9STz1WJ1ZR1mdddBzCVWb1Z9VhvWfNaLggihD+sGyXHtNH2/BUtIDl4LJ2+BXy98Hx3aj793f6Ms+JFupl0KZ6jqcdkFrqv6oj4URAgzWJ9VeyL9/HH9MhayVrO2ktyNdQq7v/Eva5/xDpGc7FDlof23agMs3Fnj5eEkyQJvZ800fZqbrHWs5axWpi+C40KmsN4y49VocHGQFn7EJ5ITG6m8P4P3KLSbhzY+NTFdlgrSJlJeFqesYRhG/u+/J9+vscyj7AvVmrXNeGUkJ3oxtBeEtqWCfD+LY1Q9F+oE+b9fSb4fGUVyE+OJBk1Iso99Cv8gySIrwycYQJKmcTNHhrP2sKYqryTmspaSHHRc1A0FET5HSGK7hDZSo3fiSEuenwXmn0aSOreQzIG9xYIbBU88UjH2TFwADQoL7/evke9H5rDekcTg4vQNPm4gPa49a1PwNrJuBx8XEN4s1kdWI1bt4HnFTibYB7AoGky22Hga7GOIOa+808GzrCLx8VSWwgFWuWo3IJmnn/IAFlNjFwJt77guk+9rJpDETDI+vB6OZ+d7GTxUpBE8GDbup/F+VPOWdcF4/5A/Zi2JjwrtvxzCiRQj67jALpIY3MHgfmhbrpLva8aRHwNvoONVGg/7tx2P1x7r5cLbB/6n4pMhZSDXWortUZvJ97OIrwKaPBeqnCQGewwotkfdIt/XjCU/Bt4gx7tuvLvB10xxvFxg0FPH8ybbS7Kfae6Ez54kY6qj6mtLMgb7jsYel22DFcGLKRLFko0Beaq+MeTHwBvseFeMh7Wx45FGrZcLDMKGZz072Wyq+o7UgqRYiGDMCNUGr1nPjJcFNmjM1d/48FA46LZ9Occ7mz12tJs63kHjWcZR1bkAvCGOh3Sq8Z6oyY6XizesZqpdRjJRR+Wh4okXzwqlaARFiV5IpFXEtFNeXjAOVVIkbsIoKiJrSIqhSPynokJ54CEV7h8tSeK89KrBizbidDHQMHh/KQ/Ai9kl4lWci4Ln/bGQCVKfXnzc0RrdZ6UXE6CawsVHmkS/rdLygnlRmqO0jb/VuCBCwL8m6IsVVrH3lCckm3uMt+doQSa4R7L4D0je13Bu+A4PfThP/DODeeHhCYrbCD7Rho/Xhl6sV1Q4JyrbRCKRSCQSiUQikUhUM18B+ZpHYuqcoggAAAAASUVORK5CYII=>

[image19]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABsAAAAaCAYAAABGiCfwAAABLElEQVR4XmNgGAU0AIxArIouSAvwDoj/QzFdwGUGOloGsugiuiCtAMgyf3RBWoAgBkQQgixcBcTRCGnqAlDwgSz7A8SaUDEQfzlcBRUBtpT4F4sYVQDI0DAsYgfRxHABVyB+D8TT0SXQgR8Dpg9AGRwkZo4mjg+AQkICXRAdnGbAtGw2FjFCgCj1IEVXsYjdgrJfIokLMkCCax8QBzAgEpMAA0TPbSB+BcTiUHEMAFIUgkXMC4jZgHgLVAwWtDAAYrNC2d1AvBtJ7gcSGw7EGLB7fxEDRPwcktgeIJ6DxEfW9xuIpaFsGTQ5sgDIAFkoWwGIvyGkUAwHxTd6tJAMviKxQT5uY0AkdfTgpRiIAPFPIH4DxMIMkGopGSonBcRPgPgxAyRuR8EoGMQAAMZfSDMPee+5AAAAAElFTkSuQmCC>

[image20]: <data:image/png;base64,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>

[image21]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB0AAAAZCAYAAADNAiUZAAABRUlEQVR4Xu2UTStFURSG36IoyscUcyaUsRETGSlGwkzJT1BMjQwoUqZmBlLyC5A/ICUmYkZMxMDXu+7am7WXzT3djG7nqadz1rv2Pat97z4XKKlnBug9/aCntD1tV3ikM7SDttFJ+pCsALagn3+mfa6XME/XTb0DHT5oMkEyb7fpT9MWUy+b+x/EBxTJVugmHXE9QXbo6fVB5Ab5AbnsL+Zoo6kXzH1VFqEDxlxebaiwT/foFR12vV8Zhz58zTeg+QU9oyf0FenOamKV7tI35H8zGdpk6sOQ/Qtd0Icd+IZDDomsW/KNWskdpIZMLWvOXV4I+Tq3XRaHDoX6MtTNXyuA1pAdmawQE8jvKmZxd9f06btdYRS6ZsrlhfAHpD9kclAiPdDXwPIC/buriU76Dh10F64byQplFtq7DdfjtF1SUlIvfAKG3lO2vMm1mAAAAABJRU5ErkJggg==>

[image22]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB0AAAAZCAYAAADNAiUZAAABJklEQVR4Xu2UsUoDURBFxyCIEUQLK6t0WltYxCofYWUngvgPsbG0DIiFnR8g1qkDwSqKiI1/YGVARUjMvew+M3s3D14swx44MHPf7kyyLGtWscgcwHf4C/uwVjz+ow0/4Cc8ljNybdn9X3BXzgp04JXrOZDLGy4jL7Dr+mfYc/0RXHP9uatLcMH+jIwG1qUPMNvIa/5DZUcDwl+mC4hmA+kDzG7y+gQuu7MzV5e4gE3JdKn2Ac3v4R18gy2XJ8FBY+lTlv6bJ8sG1V0WGx7L54IvFIdsSR4bHsuT2bRswIoeWHx4LE+CHwO9+dbVQyufE2avGqbiX5rAyNWHFl+6p2EKPzZ9TKqH/anrL/NsbratvCjI76dnNc8f4CP8hkuFKyoqKhaCCdcnW+mQcMBtAAAAAElFTkSuQmCC>

[image23]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFMAAAAaCAYAAADL5WCkAAACwUlEQVR4Xu2YS+hNURTGl/drQoS88kgxoRgoM0rJAEneocSAgYGhMlQYGEgUBkwUA4lEpMw8IpISiWLiEQN5P9fXOvu/1/3se+693ePqZv/q63/ut87Z+9y9195r/69IJpP5z+ilmspmpnXeqn4V6nY2qT6IfZeLFAPDxGI7VRtVa1SrVCsLDSjuO6y6rvqkml54TXNfun8wb6s2FNd9JCZIGCCw3vkp9VWtUw0JDyi73HVToKF7bHYRvVXfVBOchwHB9/rqvNNiGTlTbFubopqsOqNaXdyDjGSmsVEGOl3CZgcYqprBZoIRbBALJWaXhz0sW2a86qH7vFksQwNb3XVDlknsEAN6SrU2hv8a78X6fV38PV8brqEsFjimmkQeD2aKVPysWLY+Uc2nWClY3mjwu8TNFp9P9txRPedUo8jbLtbvYvIvqGaR1wyDxdp7zgHHXdU8NtshNXs/El6V3GDD8VTiO7UzqR+l/DuEIlUpaHBFwrtGXj0WqN6pDnGgBBSMRgxkowVClmPA6nFL9YzNdsCS4tnBAR7eHPLLQCaPZrMBcyVm3yNVv9pwDVfZKGG26iebCdDvXjbbAbPDg3kk4TWi1ftRfW+KTRzYItbG8Z47IsNVO9isA6o+V+zUnjlGrD+8R2WgwQcJD5kCXjof/z1gOSNLlkosVjje4JnHqlfyZ2FJgTNhij1ibWGfRBVFhW52orCk8X5M6vn9Yj7Om5WBBpcnvEWq/hKPJGHpB3AdluU+1WUX++yu67GNDWK32H52kPwy8E4ppZY8zpWI4cBeCSMlPWsnxPw7zruiOuo+++eQZWOL63EU6xQ4F/MgBl1y9wUOiMUGcaAToGP8pwAmih07An7wsN/ytpEh8EtMABmLZRiOQn4w/0VWdh2okl9Ub8SqK362w89dAJXxhVjVDNU5k8lkMplMpov4Dfoxsh1nX1dbAAAAAElFTkSuQmCC>