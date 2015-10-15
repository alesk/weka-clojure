function() {
  white.listed.countries <- c({% for country in white-listed-countries%}'{{country}}'{% if not forloop.last%},{% endif %}{% endfor %})

  manual.rule <- function(i) !i$country %in% white.listed.countries | i$emailType == 'free'


  weights <- function(i) cbind({% for stump in stumps %}
    {% squash %}{% ifequal stump.operator "=" %}
    ifelse(is.na(i${{stump.variable}}), {{stump.na_value}},
      ifelse(i${{stump.variable}} == "{{stump.threshold}}", {{stump.true_value}}, {{stump.false_value}}))

    {% else %}

    ifelse(is.na(i${{stump.variable}}), {{stump.na_value}},
      ifelse(i${{stump.variable}} {{stump.operator}} {{stump.threshold}}, {{stump.true_value}}, {{stump.false_value}}))
    {% endifequal %}{% endsquash %}{% if not forloop.last%},{% endif %}{% endfor %}
  )


  f <- function(i) (weights(i) %>% apply(1, sum)) / 2

  p <- function(i) {j <- f(i); e <- exp(j); ne <- exp(-j); e / (e + ne)}

  p.mr <- function(i) ifelse(manual.rule(i), 0.0, p(i))

  # classify without manual rule
  classify <- function(i) {p <- p(i); ifelse(p >= {{high-threshold}} , 'high', ifelse(p >= {{ambiguous-threshold}}, 'ambiguous', 'low'))}

  # classifiy with manual rule
  classify.mr <- function(i) factor(ifelse(manual.rule(i), 'low', classify(i)))

  list(explain=explain, f=f, p=p, p.mr = p.mr, weights=weights, classify=classify, classify.mr=classify.mr, manual.rule=manual.rule, white.listed.countries=white.listed.countries)
}
