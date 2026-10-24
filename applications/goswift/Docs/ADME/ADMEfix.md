My assessment: this is an ambitious engineering design, but I would not ship these algorithms as written. The strongest parts are the separation into four engines, the attempt to model multiple caffeine doses, the use of deterministic Kotlin, and the dedicated chart-processing pipeline. However, the document describes several unvalidated physiological assumptions as established science, and the implementation contains concrete bugs that could make its estimates and recommendations incorrect.

For GoSwift, I would preserve the useful mathematics and software engineering where appropriate, but change the scope from a supposedly closed-loop microdosing and safety system into a caffeine tracking and estimation engine with transparent assumptions, optional health-data context, and user-controlled reminders.
The most important issues
1. Bateman pharmacokinetic engine: good mathematical foundation, unverified parameters
   A Bateman absorption/elimination model is a legitimate mathematical approach to describing how a drug concentration changes after a dose. The problem is not the equation itself; it is the interpretation of the output and the assumptions used to configure it.
   What needs fixing
   The model does not actually calculate serum caffeine concentration. Your equation returns a modeled amount in milligrams, without a distribution volume or concentration calibration. That can be useful as an internal model output, but it cannot directly support the receptor-occupancy equation as written.

Specifically:
- activeCaffeineMg is an amount, whereas ic50Mg is being used as if it were a pharmacological concentration.
- The assumed 6-minute absorption half-life and 0.99 bioavailability for MICRODOSE_SPRAY need validation for the particular product.
- A fixed five-hour elimination half-life is a reasonable illustrative starting assumption, but individual caffeine half-lives vary substantially, with published estimates spanning approximately 1.5–10 hours in adults.


PMC
There is also a particularly relevant finding for your spray assumption. A 2025 crossover study of 14 adults found that a 60 mg caffeine mouth spray did not increase serum caffeine faster than coffee or an energy drink. That does not establish the kinetics of every possible spray formulation, but it undermines the blanket claim that mucosal sprays absorb 2.5 times faster.


PubMed



My recommendation: Retain the Bateman engine as an experimental, configurable model if you want route-specific absorption. Record the provenance of its parameters and display estimates with their assumptions. Do not present the output as an actual measurement of caffeine in the blood or a validated predictor of when sleep will be unaffected.
2. The clearance solver has a real correctness problem
   solveClearanceInstant() is intended to find the time when caffeine permanently falls below a threshold. Its current logic does not reliably establish that condition.

The relevant section is:
if (startMg <= targetThresholdMg) {
val currentMg = evaluateActiveCaffeineMg(
activeEvents, fromTime, profile, includePlanned
)
if (currentMg <= targetThresholdMg) return fromTime
}


This treats a level that is currently below the threshold as proof that it will remain below the threshold.

But a recently consumed dose can still be absorbing. Moreover, searchStart is derived from the peak time of the latest dose, not the global peak of all dose contributions. An earlier, slower-absorbing dose may still be rising after that point.

The function can therefore report that clearance has already occurred even when the projected concentration could rise above the threshold later.

Other issues to address include:
- The root solver assumes that its initial bracket contains the desired downward crossing, but that is not guaranteed for a sum of dose curves.
- It needs explicit validation of non-positive or invalid thresholds and pharmacokinetic parameters.
- It claims sub-second precision, yet the returned Instant is constructed using plusSeconds() and the stopping condition allows one-second resolution.
  Required fix: Define clearance as the last future downward crossing of the chosen modeled threshold. Search the full projected multi-dose trajectory, establish a valid bracket, and test cases where absorption is still in progress, doses have different absorption rates, or the threshold is never crossed. If the trajectory cannot be resolved reliably, return an explicit indeterminate result rather than claiming clearance.

The threshold itself also needs to be described carefully: a modeled caffeine amount below a particular number of milligrams does not guarantee that sleep will be unaffected.
3. The biometric safety gate is the biggest problem
   The most concerning defect is how missing measurements are handled.
   val hrvZ = input.latestHrvRmssdMs?.let {
   (it - input.hrvBaselineMeanMs) / safeStdDev
   } ?: 0.0


And:
val rawHrDelta =
(input.latestHeartRateBpm ?: input.restingHeartRateBaselineBpm) -
input.restingHeartRateBaselineBpm


When HRV and heart-rate data are unavailable, the code substitutes values that indicate no deviation from baseline. With other default inputs, the result can be OPTIMAL_READY and permit the maximum configured spray count.

Missing biometric data must never be interpreted as evidence of physiological readiness.

There are further scientific problems:
Current mechanism	Problem
Exercise calories increase caffeine clearance	Active calories are not a validated real-time measure of caffeine clearance. Studies of exercise and caffeine pharmacokinetics have reported differing results, including both changes and no significant effect.  PubMed +1
Hydration ratio modifies elimination	Logged water intake is not a direct measurement of dehydration or hepatic caffeine clearance.
Fixed HR and HRV cutoffs determine dose eligibility	These thresholds have not been clinically validated as a caffeine-dosing safety algorithm in this implementation.
Default HRV and sleep values fill missing data	They can turn unavailable or stale data into apparently reassuring inputs.
I would not use this engine to prescribe, increase, decrease, or authorize caffeine doses. Instead, use available biometrics to provide contextual observations, clearly identify missing or stale readings, and let the user manage their own reminders.
4. The scheduler is not yet a reliable MPC implementation
   The MicrodoseScheduleOptimizer is interesting as an optimization experiment, but it is more accurately described as a greedy rolling-horizon heuristic than a full model-predictive-control optimizer. At each candidate time, it compares dose counts using a lookahead cost; it does not jointly optimize a sequence of future controls over the full horizon.

There is also an implementation inconsistency:
val currentMgAtCursor = pkEngine.evaluateActiveCaffeineMg(
events = workingEvents,
evalTime = cursorTime,
profile = pkProfile
)


The default for includePlanned is false. However, workingEvents includes previously accepted recommendations marked isPlanned = true. As a result, this test excludes previously planned doses, while the horizon-cost calculations later explicitly include them.

That means the precondition for considering a dose is not evaluating the same trajectory as the cost function. The optimizer should use one consistent definition of which events count at each stage.

Other issues:
- priorityMultiplier on CognitiveFocusBlock is never applied.
- The three-hour bedtime cutoff and target corridor are design choices, not validated physiological thresholds.
- The proposed spray counts and target milligram levels are not established as individually safe or optimal doses.
- The daily limit, peak limit, and bedtime limit need separate definitions, validation, and tests rather than being treated as interchangeable safety constraints.
  I would not deploy this optimizer to generate automatic dosing recommendations. For the first release, schedule user-configured reminders and show the estimated consequences of logged intake. A recommendation engine should come later only if its pharmacological assumptions and intended use are properly validated.
5. The alertness engine is a heuristic, not a physiological measurement
   The two-process sleep model is an established framework for describing interactions between homeostatic sleep pressure and circadian timing.


PMC


But implementing simplified curves inspired by that framework does not automatically yield a validated measure of an individual's alertness.
In this code, the compositeAlertnessScore is calculated from selected constants, an approximate circadian function, estimated caffeine amount, and a simplified sleep-debt penalty. The Hill equation's ic50Mg also lacks the concentration units necessary for interpreting it as a receptor-blockade parameter.

These scores can be useful for exploratory visualization, but the app shouldn't label them as a clinically meaningful alertness measurement or use them to establish a safe dose.

I'd rename this component to something like AlertnessTrendEstimator, make its heuristic nature explicit, and remove phrases such as “therapeutic corridor,” “optimal parasympathetic baseline,” and “prevent cardiovascular strain” unless evidence and validation justify them.
6. The waveform processor is the most reusable part
   The LTTB downsampler and Catmull–Rom-to-Bézier conversion are sensible building blocks for a custom Compose chart. This section is much closer to a conventional engineering problem than the dosing and physiological engines.

However, three claims need correction:

LTTB does not guarantee preservation of every local maximum and minimum. It selects visually representative points by triangle area and retains the first and last samples. If exact peaks and troughs matter, consider a hybrid algorithm that explicitly preserves selected local extrema.


ggalmazor/downsampling · GitHub



Do not use Unix epoch seconds directly in Float coordinates. At present-day epoch magnitudes, floating-point precision is too coarse for fine-grained sample timing. Normalize timestamps to a relative origin and retain higher precision during processing, converting to screen-space Float only when appropriate.

The 120 Hz and allocation-free performance claims are unsupported. The processor creates lists and segment objects, and the file does not include a measured Canvas rendering path. Precompute data outside the draw phase, cache results when inputs have not changed, and benchmark on representative devices before promising frame rates.

The spline implementation also deserves property-based tests for duplicate points, unevenly spaced samples, steep transitions and boundary behavior. Centripetal parameterization is useful, but don't claim that the resulting rendered curve can never overshoot the original data.

How I would restructure the implementation
Keep the four engines as a useful software organization, but change their responsibilities:

1. Caffeine estimation

Validated input handling, timestamped dose records, explicit route assumptions, unit tests, and transparent modeled estimates. Start with the simplest defensible model; add absorption complexity only when supported by suitable data.




2. Biometric context

Read only available, permissioned Health Connect records. Track timestamps, sources and missing data. Show correlations and deviations from a user's baseline, without turning them into an unvalidated dosing safety gate.




3. Reminder scheduler

Schedule the user's chosen reminders and allow configurable quiet hours. Show transparent estimates for planned intake, rather than automatically optimizing spray counts against a claimed therapeutic corridor.




4. Signal processing

Preserve the LTTB and spline work, fix precision and extrema handling, and validate the rendered charts using correctness tests and performance benchmarks.



Recommended next steps
Before implementation, I'd prioritize:
1. Fix the clearance solver and add numerical edge-case tests.
2. Remove the false-safe behavior for missing biometric measurements.
3. Decouple activity, hydration and HRV from pharmacokinetic clearance until their relationships are adequately supported.
4. Correct the scheduler's handling of planned events and stop describing its heuristic as a proven optimal controller.
5. Validate every default constant and distinguish measured data, model assumptions and illustrative thresholds.
6. Benchmark the chart pipeline and test edge cases before claiming 120 Hz performance.
   The underlying engineering ambition is good. The issue is that the document currently presents a prototype mathematical model as a validated biomedical system.

For Hummingbird Fuel, I'd preserve the sophisticated data and visualization architecture while removing automated dosing authority. A transparent, well-tested caffeine tracker with honest estimates is a much stronger foundation than an ostensibly closed-loop system that can produce precise-looking—but unsupported—health recommendations.