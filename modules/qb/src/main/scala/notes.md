
`GROUP BY` needs to come *after* projection with an environment of input columns followed by output columns.

Projection also needs
  - `selectAll`
  - `selectDistinct(cols)`
  - `selectDistinctOn(exprs)(cols)`
