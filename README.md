# DroidRA

Android developers heavily use reflection in their apps for legitimate reasons, but also significantly for hiding malicious actions. Unfortunately, current state-of-the-art static anal- ysis tools for Android are challenged by the presence of re- flective calls which they usually ignore. Thus, the results of their security analysis, e.g., for private data leaks, are incon- sistent given the measures taken by malware writers to elude static detection. We propose the DroidRA instrumentation- based approach to address this issue in a non-invasive way. With DroidRA, we reduce the resolution of reflective calls to a composite constant propagation problem, which we model in the Constant Propagation Language (COAL). Once the COAL solver infers the values of reflection targets in an app, we instrument it to include the corresponding tradi- tional Java call for each reflective call. Our approach allows to boost an app so that it can be immediately analyzable, including by such static analyzers that were not reflection- aware. We evaluate DroidRA on benchmark apps as well as on real-world apps, and demonstrate that it can allow state-of-the-art tools to provide more sound and complete analysis results.

## Approach

Our work is directed toward a twofold aim: (1) to resolve reflective call targets in order to expose
all program behaviours, especially for analysis that must track private data; (2) to unbreak
app control-flow in the presence of reflective calls in order to allow static analyzers to produce additional results.  
Thus, we propose to automatically instrument Android apps in a way that Android state-of-the-art  static analyzers are able to analyze the app even in the presence of reflection. This instrumentation should produce an equivalent app whose analysis would be more sound and more complete.

The following figure presents an overview of the architecture of the DroidRA approach involving three modules. 
(1) The first module named JPM prepares the Android app to be inspected.
(2) The second module named RAM spots reflective calls and retrieves the values 
of their associated parameters (i.e., class/method/field names).
(3) Based on this information, the last module, named BOM, instruments the app and 
transforms it in a new app where reflective calls are augmented with standard java calls.  

![DroidRA Overview](images/fig_approach_overview.pdf)

## Evaluation

