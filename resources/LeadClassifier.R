function() {
  white.listed.countries <- c({% for country in white-listed-countries%}'{{country}}'{% if not forloop.last%},{% endif %}{% endfor %})

  manual.rule <- function(i) !i$country %in% white.listed.countries | i$emailType == 'free'

  f <- function(i) apply(cbind({% for stump in stumps %}
    {% squash %}{% if stump.operator "=" %}
    ifelse(i${{stump.variable}} == "{{stump.threshold}}", {{stump.true_value}}, {{stump.false_value}})

    {% else %}

    ifelse(i${{stump.variable}} {{stump.operator}} {{stump.threshold}}, {{stump.true_value}}, {{stump.false_value}})
    {% endif %}{% endsquash %}{% if not forloop.last%},{% endif %}{% endfor %}
  ), 1, sum)/2

  p <- function(i) {j <- f(i); e <- exp(j); ne <- exp(-j); e / (e + ne)}

  # classify without manual rule
  classify <- function(i) {p <- p(i); ifelse(p >= {{high-threshold}} , 'high', ifelse(p >= {{ambiguous-threshold}}, 'ambiguous', 'low'))}

  # classifiy with manual rule
  classify.mr <- function(i) factor(ifelse(manual.rule(i), 'low', classify(i)))

  list(explain=explain, f=f, p=p, classify=classify, classify.mr=classify.mr, manual.rule=manual.rule, white.listed.countries=white.listed.countries)
}
