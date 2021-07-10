# hot-variables-analysis

A simple backwards control flow analysis (Query Count Estimation or QCE) implementation in soot to find the set of Hot Variables at each location in a program. At each program location l, QCE pre-computes a set H(l) of “hot variables” that are likely to cause many queries to the solver during symbolic execution if they were to contain symbolic values.
