# Zoe Nude Automated Cosmetics Network: Unified Technical & Business Blueprint

## 1. Executive Vision: Distributed Cloud-Managed Beauty Retail

Traditional automated retail locks operators into an expensive hardware trap: every vending kiosk duplicates a desktop-grade computer, large touchscreen, EMV payment terminal, bill acceptor, cellular modem, and heavy motorized grid-powered infrastructure. When applied to cosmetics, this capital cost restricts placement to mega-venues (international airports and Tier-1 malls) whose lease fees erase retail margin gains.

The Zoe Nude architecture inverts that model by stripping intelligence out of the physical dispenser and splitting the business into three clean layers:

* **The Zoe Nude App (The Store):** Handles all product discovery, shade education, digital checkout, payment pre-authorization, loyalty, and personalized recommendations on the consumer's smartphone.
* **The Zoe Nude Cloud (The Brain):** Centralizes product catalogs, dynamic pricing, cryptographic authorization, inventory ledgers, demand analytics, and predictive route replenishment.
* **The Zoe Nude Dispenser (The Self-Powered Endpoint):** An uncorded, screenless, modem-free, zero-grid mechanical fulfillment unit powered by a unified tri-source energy architecture—**Passive NFC**, **Indoor/Outdoor Solar**, and a **Fun Hand-Crank Music Box**—designed to work reliably everywhere from windowless corridors to outdoor pop-ups.

| Retail & Infrastructure Dimension | Brick-and-Mortar Cosmetics | DTC E-Commerce | Legacy Smart Vending | Zoe Nude Tri-Source Network |
| --- | --- | --- | --- | --- |
| Primary Customer Interface | Physical testers and beauty staff | Web browser or mobile app | 10–32 inch onboard touchscreen | Consumer smartphone running Zoe Nude App |
| Network Connectivity | Commercial store broadband | Consumer internet | Dedicated 4G/5G SIM modem per machine | Consumer smartphone acts as cloud relay |
| Payment Hardware | Retail POS terminals | Cloud gateway (Apple Pay, cards) | Physical EMV reader and bill validator | In-app pre-authorization; zero machine payment hardware |
| Endpoint Electrical Draw | High commercial AC (HVAC, lights, POS) | None at endpoint | 110–240 V AC outlet (100–500 W) | 0 W grid draw; Passive NFC + Solar + Hand Crank |
| Last-Mile Fulfillment | Store freight, retail leases, store labor | Individual parcel shipping, void fill, breakage returns | Route restocking plus heavy kiosk depreciation | Rapid cartridge swap; zero individual parcel shipping |
| Viable Deployment Footprint | 500–3,000 sq. ft. storefront | Fulfillment warehouse | 15–25 sq. ft. near AC outlet | 1–3 sq. ft. wall or counter; installs anywhere |

Eliminating five structural cost layers—individual parcel shipping, retail storefronts, endpoint computers/modems, checkout terminals, and duplicated software—unlocks micro-venues (boutique hotels, fitness studio locker rooms, university dorms, coworking lounges, salons, and residential lobbies) that traditional retail cannot profitably serve. Positioned around *"Zoe Nude Cosmetics: Beauty. Without the Baggage. / Beauty. On Your Terms."*, the network combines a cosmetics brand, a mobile commerce platform, and a distributed automated retail footprint.

---

## 2. End-to-End User Experience & Zero-Trust Cryptographic Flow

Because the dispenser has no cellular modem and sits in public spaces, security relies on a zero-trust model: the smartphone acts purely as an untrusted data pipe between the cloud and the dispenser's hardware cryptography. A printed QR code or passive NFC NDEF tag on the chassis only identifies the `Machine_ID`—it holds zero dispensing authority.

The customer journey merges a tactile **4-Step Self-Powered Interaction** with a **7-Stage Atomic Cloud-to-Hardware Protocol** so the buyer is never charged unless physical delivery is verified:

1. **Identify & Discover (Pre-Step 1):** The customer scans the machine's QR code or taps the NFC target. The app reads the `Machine_ID` and fetches real-time SKU inventory, shades, and pricing from the Zoe Nude Cloud, placing a short checkout lock on the selected unit.
2. **Step 1 — Phone Authorizes (Purchase & Cloud Auth):** The customer pays in the app, triggering a **payment pre-authorization hold** (not an immediate fund capture). The cloud generates a single-use, expiring, AES-128 encrypted `AUTH_TOKEN` binding the `Machine_ID`, `Column_ID`, and a monotonically increasing transaction `Nonce`.
3. **Step 2 — NFC Unlocks:** The customer taps their phone against the dispenser's NFC zone.
* *Charged Path (Solar or Prior Crank Available):* If the onboard 1.0 F supercapacitor holds charge above 2.5 V, the controller runs in active supply mode and trips the target SKU column's bistable selector clutch in under 300 ms [cite: 364].
* *Zero-Light / Empty-Cap Fallback (Passive NFC):* Even in a pitch-black 0 lux room with a completely discharged 0 V supercapacitor, the phone's 13.56 MHz RF field harvests ~23 mJ (~3 mJ for MCU/AES verification plus ~20 mJ to trip the tiny bistable selector pawl) in under 0.8 s [cite: 364, 433, 443]. **NFC never powers heavy product ejection directly; it simply unlocks the crank-to-column mechanical coupling for the paid SKU.**


4. **Step 3 — Customer Turns the Crank:** The customer winds the side-mounted 45 mm crank (~90 RPM for ~4 seconds). That single motion performs three simultaneous jobs:
* *Direct Mechanical Dispensing:* Human wrist power drives the unlocked rotary rocker escapement cam (~0.40 W peak mechanical drag) to singulate and drop the cosmetic carton cleanly with zero electrical motor drain.
* *Signature Music Box Experience:* Mechanical rotation drives a custom 18-note Sankyo music-box movement (~0.30 W drag) bolted to an internal sounding board, playing Zoe Nude's signature melody [cite: 412].
* *Electrical Recharge & Reveal Lighting:* A step-up gearbox and micro-generator harvest ~0.843 W net electrical power even during active dispensing (~3.371 J over 4 seconds)—immediately powering a 6-second warm-white LED vanity reveal inside the retrieval bay (540 mJ) and banking a multi-joule surplus for future transactions.


5. **Step 4 — Product Dispenses & Reports:** The released item drops less than 75 mm onto a zero-power `DT1-028K` PVDF piezoelectric film sensor at the base of the chute [cite: 389, 390]. The controller samples the impact waveform via its 12-bit SAR ADC (~1 mJ), increments its non-volatile stock record, and creates an AES-signed `DISPENSE_RECEIPT` (`9000h`). Because Step 3 guarantees multi-joule supercapacitor reserves, an ultra-low-power Bluetooth Low Energy (BLE) transceiver (`nRF52832`) automatically pushes the signed receipt back to the customer's phone (~15 mJ total handshake) without requiring a second NFC tap [cite: 399, 400]. (On BLE-less builds, or if the link drops, the receipt saves to non-volatile memory and syncs on the next NFC tap.) The app forwards the receipt to the cloud to capture the held payment and update inventory.

| UX Step | Cloud / Protocol Stage | Hardware & RF Layer | Energy Source Used | Security & Fault Safeguard |
| --- | --- | --- | --- | --- |
| Pre-Step 1 | 1. Identify & 2. Discover | QR / NDEF URI + HTTPS TLS 1.3 | Consumer phone only | Static tag carries `Machine_ID` only; zero unlock privilege |
| Step 1: Phone Authorizes | 3. Purchase & 4. Authorize | App wallet + Cloud KMS AES-128 | Consumer phone & cloud | Payment pre-auth hold; single-use `AUTH_TOKEN` + monotonic nonce |
| Step 2: NFC Unlocks | 5a. Authenticate & Unclutch | 13.56 MHz NFC -> MCU AES + bistable pawl | Solar/Crank supercap OR Passive NFC (~23 mJ) | Ideal-diode OR-ing prevents empty supercap from loading RF boot [cite: 385, 386] |
| Step 3: Customer Cranks | 5b. Dispense, Play & Recharge | Rotary cam + 18-note music box + dynamo | Human wrist (2.33 W mech) -> 3.37 J elec | Crank disconnected from columns until Step 2; one-way & slip clutches [cite: 404] |
| Step 4: Product Dispenses | 6. Confirm & 7. Cloud Capture | `DT1-028K` Piezo ADC -> BLE / NFC -> HTTPS | Crank/Solar surplus (~556 mJ LED + BLE) [cite: 399] | Receipt stored in NVM if phone leaves early; payment auto-voids if no drop |

### Mobile OS NFC Optimization

Standard Android foreground dispatch emits tiny ~20 us polling spikes that harvest negligible energy. The Zoe Nude App must use `NfcAdapter.enableReaderMode()` (with `FLAG_READER_NFC_A` and `FLAG_READER_SKIP_NDEF_CHECK`) on Android and `NFCTagReaderSession` on iOS to hold a continuous carrier field during Step 2 [cite: 423]. Crucially, enabling `FLAG_READER_NO_PLATFORM_SOUNDS` suppresses the default OS chime at initial contact—preventing users from pulling their phone away prematurely during a cold-start passive harvest—while setting `EXTRA_READER_PRESENCE_CHECK_DELAY` to 250 ms prevents aggressive firmware presence checks from dropping the link during actuation [cite: 423].

---

## 3. Tri-Source ("Works Everywhere") Electrical & Silicon Architecture

Combining **Passive NFC**, **Indoor/Outdoor Solar**, and **Hand-Crank Generation** onto one board requires careful component selection, conflict-free power-path OR-ing, and an explicit silicon transition plan due to recent semiconductor lifecycle changes.

### Controller Silicon Strategy: Prototyping (`NGC1081`) vs. Production (`ST25DV04KC` + `nRF52832`)

* **Phase 1 Prototyping (`EVAL_NGC1081` / `NAC1080`):** Infineon's `NGC1081` (32-pin, 28 MHz Arm Cortex-M0, 60 kB NVM, 16 kB RAM, 12-bit SAR ADC, 10-bit DAC, UART/I2C/SPI, integrated 250 mA H-bridge, AES/TRNG, and dual active/passive supply modes [cite: 364, 443]) and `NAC1080` (16-pin smart-lock variant [cite: 433, 435]) provide an all-in-one benchtop platform for rapid firmware and passive RF testing. However, **Infineon issued Product Discontinuation Notifications in March 2025 placing `NGC1081XTMA1` at End of Life (EOL)**—only remaining distributor stock (such as 3,585 units at Mouser at $3.81 qty 1 / $2.29 at 1,000) is available before permanent depletion [cite: 445, 472].
* **Phase 2/3 Production-Safe Dual-Chip Architecture:** To avoid building commercial volume on EOL silicon, the production board pairs two active, high-volume chips:
1. **STMicroelectronics `ST25DV04KC` (or `ST25DV16KC`):** An ISO 15693 / NFC Forum Type 5 dynamic tag IC with 1 MHz I2C, 256-byte Fast Transfer Mode buffer, 1.8–5.5 V supply, and an analog **`V_EH` energy-harvesting output pin** (`EH_MODE = 00h` at boot) [cite: 456, 457, 459, 461]. Its configurable **`GPO` interrupt pin** wakes the host MCU upon RF field detection, and its `LPD` (Low Power Down) pin cuts VCC standby draw below 1 uA (or 0 uA when powered strictly by RF during pure passive fallback) [cite: 456, 457, 460]. Because smartphone RF energy delivery on `V_EH` varies by handset and antenna geometry (best paired with a large Class-1 loop antenna and local tank capacitor [cite: 458]), the `V_EH` rail is dedicated solely to charging the 23 mJ emergency clutch-unlock pulse.
2. **Nordic Semiconductor `nRF52832`:** A 64 MHz Arm Cortex-M4F multiprotocol SoC (512/256 kB Flash, 64/32 kB RAM, 128-bit AES accelerator, 12-bit 200 ksps ADC, 1.7–3.6 V supply) [cite: 399, 400]. It consumes just **0.3 uA** in deep sleep without RAM retention (**1.6 uA** idle with 32 kHz RTC) and only **5.3 mA** TX (0 dBm) / **5.4 mA** RX for BLE Step 4 receipt delivery, while also offering built-in 13.56 MHz NFC-A wake-on-field sensing [cite: 399, 400].



### Solar Subsystem (`AEM10941` + Indoor/Outdoor PV)

* **Energy Harvester PMIC:** The **e-peas `AEM10941**` (5 x 5 mm QFN28 requiring only 7 passive external components; $3.17 qty 1 / $2.37 qty 10 with 5,573 in stock at Mouser [cite: 370, 466]) extracts power across an ultra-wide **50 mV to 5 V** input range, **cold-starting from 380 mV and just 3 uW** [cite: 369, 370]. It executes open-circuit MPPT sampling every 5 seconds (selectable 70%, 75%, 85%, or 90% $V_{\text{OC}}$), integrates dual regulated LDOs (1.2/1.8 V up to 20 mA and 1.8–4.1 V up to 80 mA), and includes an integrated balun and configurable overcharge/overdischarge protection for supercapacitors [cite: 369, 370, 371].
* **PV Chemistry by Venue Tier:**
* *Interior Corridors & Lobbies (50–500 lux):* **Epishine `LEH3**` organic PV (~18 +/- 2 uW/cm2 at 500 lux [cite: 452]) or **Lightricity** indoor PV (22.1 uW/cm2 at 200 lux [cite: 430]) maximize low-lux indoor LED/fluorescent capture.
* *Sunlit Windows & Outdoor Pop-Ups (>1,000 lux):* Indoor-grade **Panasonic Amorton `AM-1816**` amorphous silicon modules (96.7 x 56.7 x 1.1 mm, 15.6 g, 252 uW at 3.0 V / 84 uA indoor rating, 4.9 V / 94 uA at 200 lux [cite: 425, 427, 428]) are specified for use below 1,000 lux, whereas Panasonic's **outdoor-grade Amorton** glass/film cells deliver 5.6–7.89 mW/cm2 under AM-1.5 sunlight [cite: 424]. Outdoor units must specify outdoor-rated a-Si rather than indoor OPVs to avoid UV/sunlight degradation, protected by `AEM10941` overvoltage clamping.



### Hand-Crank Drivetrain & `LTC3129` Power Conditioning

With a compact crank radius $r = 45\text{ mm}$ and a gentle tangential force $F = 5.5\text{ N}$ turned at 90 RPM ($\omega = 9.425\text{ rad/s}$), input shaft torque and mechanical power are:

$$\tau = 5.5\text{ N} \times 0.045\text{ m} = 0.2475\text{ N}\cdot\text{m}, \quad P_{\text{mech}} = \tau\omega = 2.333\text{ W}$$

Passing the generator share through a back-drivable 1:12 step-up gearbox ($\eta = 0.85$), permanent-magnet DC micro-generator ($\eta = 0.75$), low-drop Schottky rectifier bridge ($\eta = 0.88$), and an **Analog Devices `LTC3129**` micropower synchronous buck-boost converter ($\eta = 0.92$, 2.42–15 V input, 200 mA output, 1.3 uA quiescent current, integrated Maximum Power Point Control [cite: 377, 378, 379, 381]) yields a combined chain efficiency of $\eta_{\text{chain}} = 0.85 \times 0.75 \times 0.88 \times 0.92 \approx 51.6\%$:

* **During Active Dispensing (Step 3):** Subtracting 0.30 W (music box) and 0.40 W (dispensing cam) leaves 1.633 W into the generator drivetrain, producing **0.843 W net electrical output** (**3.371 J** over a 4-second crank).
* **During Free-Spin Cranking:** Subtracting only 0.30 W (music box) yields **1.049 W net electrical output** (**4.196 J** over 4 seconds).

Using the `LTC3129`'s MPPC tracking prevents a discharged 0 V supercapacitor from short-circuiting the dynamo—eliminating Lenz's Law electromagnetic braking so the crank feels consistently smooth at every state of charge [cite: 378, 381].

### Conflict-Free Tri-Source OR-ing (`MAX40200`) & Supercapacitor Bank (`SCMR18F105SRBA0`)

As highlighted in Infineon's `EVAL_NGC1081` manual, connecting an active 3.0–3.6 V power rail while an NFC reader field is simultaneously energized creates a **conflicting power source hazard** [cite: 364]. Moreover, wiring an empty 1.0 F supercapacitor directly across the NFC harvesting pin would swallow the smartphone's 20–50 mW RF field and prevent cold boot.

To isolate all three energy domains cleanly, the power bus uses **Analog Devices `MAX40200` 1 A ideal diodes** (1.5–5.5 V supply, <43 mV drop at 500 mA, 85 mV at 1 A, 7 uA typical quiescent current, and **70 nA typical reverse-bias leakage**, blocking up to 6 V in either direction when disabled [cite: 385, 386]):

1. Both the `AEM10941` solar charger and `LTC3129` crank converter feed a shared **KYOCERA AVX `SCMR18F105SRBA0**` supercapacitor (`1.0 F`, `5.5 V`, `0.25 Ohm` ESR, `18 mm` radial, active stock of 2,290 units at Mouser [cite: 387]), reverse-isolated via ideal-diode OR-ing.
2. When the supercapacitor sits between `2.5 V` and `5.0 V`, the regulated active rail powers the `nRF52832` / selector system through a `MAX40200`, keeping the NFC tag port strictly isolated for data/wake-up. When the supercapacitor drops below `2.5 V` (0 lux depleted state), the `MAX40200` disables and reverse-blocks (`70 nA`), allowing the smartphone's RF field to charge a dedicated small `V_EH` / `VCC_HB` tank capacitor in under 0.8 s to fire the 23 mJ selector pawl without leaking a microamp into the empty 1.0 F reservoir [cite: 385, 386].

For a `1.0 F` bank derated to a `5.0 V` ceiling and `2.5 V` cutoff:

$$E_{5.0\text{V}} = \frac{1}{2}(1.0\text{ F})(5.0\text{ V})^2 = 12.500\text{ J}, \quad E_{2.5\text{V}} = 3.125\text{ J}, \quad E_{\text{usable}} = 9.375\text{ J}$$

* **Cold-Start & Charge Speed:** Free cranking reaches the `2.5 V` active threshold in **2.98 s** (~4.5 turns) and full `5.0 V` charge in **11.92 s**.
* **Standby Retention:** Combining supercapacitor self-discharge (~15 uA typical; 60 uA conservative high-temperature case), `LTC3129` quiescent current (1.3 uA [cite: 379]), and `nRF52832` / `ST25DV` sleep (2.0 uA [cite: 399, 460]) yields a total idle drain of **18.3 uA typical** (**63.3 uA hot**), holding charge for **37.9 hours (1.58 days) typical** and **11.0 hours (0.46 days) hot**.
* **Per-Vend Energy & "Works Everywhere" Solar Matrix:** A full illuminated BLE hybrid vend requires **593 mJ** (25 mJ selector pawl + 12 mJ MCU + 540 mJ 6 s LED reveal + 16 mJ piezo/BLE)—meaning a single 4-second dispensing crank (3.371 J) funds **5.68 illuminated BLE vends**, and a full 9.375 J reservoir supports **15.8 illuminated BLE vends**. Across a 100 cm2 solar panel (85% PMIC efficiency, 12 h/day illumination):

| Environment | Illuminance | PV Power Density | Net Solar Power (100 cm2) | Daily Solar Energy (12 h/d) | Daily Vends from Solar Alone | System Operation Mode |
| --- | --- | --- | --- | --- | --- | --- |
| Dark Closet / Basement | 0 lux | 0 uW/cm2 | 0.000 mW | 0.00 J/d | 0.0/d | Passive NFC unlocks (~23 mJ); 4 s crank harvests 3.37 J for LED + BLE + surplus |
| Dim Hotel Corridor | 50 lux | 2.0 uW/cm2 | 0.170 mW | 7.34 J/d | 12.4/d | `AEM10941` harvests down to 3 uW [cite: 369]; cranking tops off reserve |
| Retail / Gym Lobby | 200 lux | 8.0 uW/cm2 | 0.680 mW | 29.38 J/d | 49.5/d | Supercap stays charged continuously; instant <300 ms NFC unlock + crank |
| Bright Vanity / Office | 500 lux | 18.0 uW/cm2 [cite: 452] | 1.530 mW | 66.10 J/d | 111.5/d | Continuous energy surplus supporting frequent BLE beacons and reveals |
| Sunny Window / Outdoor | 20,000+ lux | 1,250 uW/cm2 (clamped) | 106.250 mW | 4,590.00 J/d | 7,740.3/d | Outdoor Amorton a-Si + `AEM10941` 5 V clamping protects supercap [cite: 369, 424] |

---

## 4. Mechatronic Dispensing, Music-Box Acoustics & Closed-Loop Verification

### Cosmetics Fragility & Standardized Gravity Cartridges

Traditional helical-coil vending drops items 300–900 mm onto rigid trays, shattering pressed powder compacts (which frequently fracture above 150–200 mm drops), cracking glass serum bottles, and scuffing luxury cartons. Zoe Nude eliminates impact trauma via **vertical gravity-fed modular cartridges** holding products inside two standardized recyclable paperboard carton sizes (**Size S** for lipsticks, mascaras, pencils; **Size M** for compacts and 30 mL serums). Every column dispenses from its bottommost slot onto an elastomer-lined ramp with a fixed **<75 mm drop**.

### Rotary Escapement, Selective Clutching & Anti-Tamper Crank Drivetrain

1. **NFC-Gated Column Selector:** A dual-finger rotary rocker escapement isolates the weight of the stacked product column (~8.8 N across 15 units) before dropping the bottom carton. When idle, all column cams sit mechanically decoupled from the crank shaft. During Step 2, the unlocked SKU's bistable selector pawl (~20–25 mJ) engages a dog clutch onto the crank drive spline for a single revolution, returning to lock as soon as delivery completes—guaranteeing zero unpaid dispensing if passersby spin the crank.
2. **Custom 18-Note Music Box & Acoustic Soundboard:** The crank shaft drives a mechanical **Sankyo 18-note movement** ($917 custom cylinder mold, $2.05/unit basic or $3.51/unit gold at MOQ 100, ~20 workdays melody engineering + ~40 days mold fabrication; compared to $260–$2,020 for motorized programmable `Muro Box` units [cite: 412, 413, 418]). Bolting the movement onto an internal tonewood or composite sounding board behind carved front-bezel acoustic slots projects a warm chime rather than muffled steel enclosure ringing.
3. **One-Way Reverse Protection (`INA HF0812`):** Turning a hand-crank music box backwards forces cylinder pins underneath the tuned steel comb teeth and snaps them. Pressing an **INA `HF0812` drawn-cup needle roller one-way clutch** (8 mm bore, 12 mm OD, 12 mm width, **3.15 Nm permissible torque**, 17,000 RPM limit, -10 to +70 deg C, 3.5 g [cite: 404, 405, 408]) onto a hardened shaft ahead of the music box lets backward cranking freewheel harmlessly—while a full-wave bridge on the generator still harvests energy regardless of rotation direction.
4. **Over-Torque Vandalism Slip Clutch:** A preset friction or magnetic torque limiter behind the free-spinning 45 mm handle slips cleanly above 1.5–2.0 Nm, shielding the 1:12 gearbox, `HF0812` clutch, and music-box drum from violent yanking.

### Zero-Power Drop Verification (`DT1-028K`)

To confirm physical delivery with 0 W standby draw (avoiding 15–17 mA optical IR sensors that foul with cosmetic dust), the elastomeric chute mounts a **TE Connectivity `DT1-028K**` 28 um PVDF piezoelectric film sensor (12 x 30 mm active electrode area, 1.36–1.38 nF capacitance, 14.4 V/N open-circuit sensitivity, 0 to +70 deg C operating range [cite: 389, 390, 393]). Clamped through a 3.3 V Zener/TVS array into a 10 MOhm ADC load (`f_c = 1 / (2 * pi * R * C)` yielding a ~13.6–13.8 ms decay envelope [cite: 390, 392]), falling items trigger a crisp ADC capture (~1 mJ), backed up by a micro-switch flap for ultra-light (<5 g) refills. *(Supply-chain note: the riveted `1-1002908-0` catalog variant is marked discontinued at select distributors [cite: 393], so production BOMs should lock in active TE lead-attached `DT1-028K` stock [cite: 391, 398].)*

---

## 5. Cloud Telemetry Schema & Cartridge Swap Operations

Every completed purchase uploads a strict privacy-preserving packet via the consumer's smartphone—never collecting contacts, background GPS, or invasive device identifiers:

| Telemetry Field | Data Format | Origin | Business & Maintenance Purpose |
| --- | --- | --- | --- |
| `Machine_ID` | UUID (16 bytes) | QR / NFC URI | Identifies physical machine, venue partner, and micro-zone |
| `SKU_Column_ID` | UINT8 + SKU | Cloud + Dispenser NVM | Tracks shade velocity per location and decrements column count |
| `Transaction_Timestamp` | INT64 (UTC ms) | Cloud Auth + Phone Relay | Captures hourly/day-of-week demand curves |
| `Sale_Price_Net` | UINT32 (Cents) | Cloud Pricing | Revenue analytics, dynamic A/B pricing, venue revenue share |
| `Dispense_Confirmation` | AES-128 MAC + ADC Peak | Hardware AES + `DT1` Piezo | Proves physical drop prior to payment capture; detects carton mass anomalies |
| `Hardware_Health_Flags` | UINT16 Bitmask | MCU Diagnostics | Tracks supercap voltage, solar recharge rate, and clutch cycles |
| `Machine_Stock_Array` | UINT8[16] | Non-Volatile Memory | Keeps physical magazine counts synchronized with cloud ledger |
| `Customer_Token_Anon` | SHA-256 Hash | App Auth Layer | Powers loyalty points and repeat shade recommendations without PII |

Route restocking takes under 60 seconds via a **Cartridge Swap Protocol**: technicians authenticate via "Operator Mode" in the app, tap NFC to unlock the service door latch, swap empty metal cartridges for factory-packed refills, and tap once to write lot numbers, FDA/EU cosmetic expiration dates, and reset column stock counts.

---

## 6. Production BOM Snapshot & Phased Engineering Roadmap

| Subsystem | Prototype / Early Stock Part | Production Active Part | Key Verified Spec & Cost / Status Notes |
| --- | --- | --- | --- |
| NFC & Host MCU | Infineon `NGC1081` (`DEV_KIT_NGC1081`) | `ST25DV04KC` + Nordic `nRF52832` | `NGC1081` is EOL (PDN Mar 2025; 3,585 left at Mouser @ $2.29/1k) [cite: 472]; `ST25DV04KC` (`V_EH` + `GPO`) + `nRF52832` (BLE + AES + 0.3 uA sleep) is active [cite: 399, 457] |
| Solar PMIC | TI `bq25570` / e-peas `AEM10941` | e-peas `AEM10941` | 380 mV / 3 uW cold start, 5 s MPPT, QFN28; $3.17 (qty 1) / $2.37 (qty 10), 5,573 in stock at Mouser [cite: 369, 466] |
| Solar Panel | `Epishine LEH3` / `AM-1816` | `LEH3` (indoor) / Amorton outdoor a-Si | `LEH3` ~18 uW/cm2 @ 500 lux [cite: 452]; `AM-1816` 96.7x56.7 mm 252 uW indoor; outdoor Amorton 5.6–7.89 mW/cm2 @ AM-1.5 [cite: 424, 425] |
| Crank Buck-Boost | ADI `LTC3129` | ADI `LTC3129` | 2.42–15 V input, 200 mA buck-boost, 1.3 uA $I_Q$, integrated MPPC [cite: 377, 379] |
| Power-Path OR-ing | Schottky diode test | ADI `MAX40200` (2x–3x) | 1.5–5.5 V, <43 mV @ 500 mA, 70 nA reverse leakage, 6 V off-block [cite: 385, 386] |
| Supercapacitor | `SCMR18F105SRBA0` | KYOCERA AVX `SCMR18F105SRBA0` | 1.0 F, 5.5 V, 0.25 Ohm ESR, 18 mm radial; 2,290 in stock at Mouser [cite: 387] |
| Music Box | Stock 18-note Sankyo | Custom 18-note Sankyo drum | $917 custom mold NRE, $2.05/unit basic @ MOQ 100, ~60-day lead time [cite: 412] |
| One-Way Clutch | `HF0812` | INA `HF0812` | 8 x 12 x 12 mm drawn-cup needle clutch, 3.15 Nm torque, 17,000 RPM [cite: 404, 405] |
| Drop Sensor | `DT1-028K` piezo film | Active `DT1-028K` + micro-switch | 28 um PVDF, 1.36–1.38 nF, 14.4 V/N; avoid discontinued `1-1002908-0` [cite: 389, 390, 393] |

### Four-Phase Implementation Roadmap

1. **Phase 1 — Benchtop Proof-of-Concept (`EVAL_NGC1081` + Crank Rig):** Procure `DEV_KIT_NGC1081` [cite: 364], a 1:12 geared PM DC generator, `LTC3129` demo board (`DC2393A`) [cite: 377, 478], `AEM10941` EVK [cite: 371], `SCMR18F105SRBA0` [cite: 387], `HF0812` clutch [cite: 404], and `DT1-028K` piezo element [cite: 391]. Validate passive 23 mJ selector-clutch tripping through 4 mm phone cases using `enableReaderMode` (`FLAG_READER_NO_PLATFORM_SOUNDS`, 250 ms delay) [cite: 423], confirm smooth MPPC crank feel from 0 V to 5.0 V [cite: 378], and verify `MAX40200` conflict isolation between Active and Passive modes [cite: 364, 385].
2. **Phase 2 — Production Dual-Chip Mainboard (`ST25DV04KC` + `nRF52832`):** Design the commercial 4-layer PCB replacing EOL `NGC1081` [cite: 472] with `ST25DV04KC` (`V_EH` + `GPO` + I2C) [cite: 456, 457] and `nRF52832` (BLE receipt push + AES-128 + ADC + MOSFET matrix control) [cite: 399], paired with the `AEM10941` + `LTC3129` + `MAX40200` tri-source power tree [cite: 369, 378, 385].
3. **Phase 3 — Mechatronic Chassis, Custom Melody & Packaging Validation:** Commission the $917 custom 18-note Sankyo melody drum (~60-day lead time) [cite: 412], design the tonewood/composite sounding board and slip-clutched 45 mm crank assembly with `INA HF0812` reverse protection [cite: 404], standardize Size S/M secondary cartons with <75 mm drop chutes, and tune the 13.56 MHz NFC and 2.4 GHz BLE antennas inside the final enclosure.
4. **Phase 4 — Pilot Deployment & End-of-Line QA:** Deploy dual-tier pilots (`Epishine LEH3` for indoor hotel/gym corridors [cite: 452]; outdoor-grade Amorton a-Si for sunlit windows/outdoor pop-ups [cite: 424]). Enforce end-of-line manufacturing tests on every unit: 0 V depleted passive NFC cold-start verification, 72-hour dark-room soak recovery, chute piezo sensitivity calibration, and slip-clutch breakaway torque verification.

I've created a new, clean unified report consolidating everything learned and built across the project into a single coherent architecture:

* **Unified 4-Step UX & 7-Stage Cryptographic Protocol:** Integrated your 4-step dispenser diagram (*Phone authorizes -> NFC unlocks -> Customer turns the crank -> Product dispenses*) directly into the atomic payment pre-authorization and hardware verification loop, with BLE (`nRF52832`) receipt return and zero-power passive NFC fallback.
* **Tri-Source ("Works Everywhere") Power Architecture & Updated BOM:** Combined **Passive NFC**, **Indoor/Outdoor Solar (`AEM10941` + `Epishine LEH3` / `Panasonic Amorton`)**, and **Hand-Crank Generation (`LTC3129` + `1:12` PM DC dynamo)** using **`MAX40200`** ideal-diode OR-ing to eliminate `Active/Passive` power conflicts and prevent an empty `1.0 F` supercapacitor from stalling RF cold-starts.
* **Lifecycle & Supply-Chain Corrections:** Flagged that **`NGC1081XTMA1` is End of Life (March 2025 PDN)**—preserving it strictly for Phase 1 `EVAL_NGC1081` prototyping while specifying an active production path using **`ST25DV04KC` + `nRF52832**`, an active in-stock **`SCMR18F105SRBA0`** (`1 F, 5.5 V`) supercapacitor, and active **`DT1-028K`** piezo sourcing.
* **Consolidated Mechatronics & Acoustic Design:** Merged the standardized Size S/M gravity cartridges (<75 mm drop), NFC-gated column selector clutching, custom 18-note Sankyo music box ($917 mold NRE, $2.05/unit @ MOQ 100) with sounding board amplification, `INA HF0812` one-way reverse-crank protection, and over-torque slip clutching into one clean, contradiction-free blueprint.