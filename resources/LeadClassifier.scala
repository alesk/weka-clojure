package com.toptal.analytics.leadclassification

/**
 * LogitBoost Classifier with decision stumps
 *
 * Created: {{created-at}}
 *
 **/

object LeadClassifiers {
  val Default = LeadClassifier(
    ruleDefs = Vector({% for stump in stumps %}
      {% squash %}{% ifequal stump.operator = %}
      NominalRuleDef(FeatureDefs.{{stump.variable}}, "{{stump.threshold}}", {{stump.true_value}}f, {{stump.false_value}}f)
      {% else %}
      NumericRuleDef(FeatureDefs.{{stump.variable}}, {{stump.threshold}}, {{stump.true_value}}f, {{stump.false_value}}f)
      {% endifequal %}{% endsquash %}{% if not forloop.last%},{% endif %}{% endfor %}
    ),
    highThreshold = {{high-threshold}},
    ambiguousThreshold = {{ambiguous-threshold}},
    manualRule = ManualRule(whitelistedNamesLower = Set({% for country in white-listed-countries %}
      "{{country}}"{%if not forloop.last%},{%endif%}{% endfor %}
    ))
  )
}
