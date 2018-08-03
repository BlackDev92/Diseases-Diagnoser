package pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.rule;

import com.google.common.collect.Range;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Range.*;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.utils.Constants.*;

public class RuleBuilder {

    private Variable patientVariable;
    private Variable ageVariable;

    private String name;

    private Set<Entity> symptoms = new HashSet<>();
    private Set<Entity> negativeTests = new HashSet<>();
    private Set<Entity> diseases = new HashSet<>();

    private AbstractAtom patientDeclarationAtom;
    private AbstractAtom ageAtom;

    private Range<Integer> ageRange = Range.all();

    public RuleBuilder() {
        this.patientVariable = new Variable("patient");
        this.ageVariable = new Variable("_age");
        this.ageAtom = new TwoArgumentsAtom<>(AGE_PROPERTY, patientVariable, ageVariable);
    }

    public RuleBuilder(Entity patientClass) {
        this.patientVariable = new Variable("patient", patientClass);
        this.ageVariable = new Variable("_age");
        this.patientDeclarationAtom = new ClassDeclarationAtom<>(patientClass, patientVariable);
        this.ageAtom = new TwoArgumentsAtom<>(AGE_PROPERTY, patientVariable, ageVariable);
    }

    public RuleBuilder(Rule rule) {
        for (AbstractAtom declarationAtom : rule.getDeclarationAtoms()) {
            if (declarationAtom instanceof ClassDeclarationAtom) {
                ClassDeclarationAtom classDeclarationAtom = (ClassDeclarationAtom) declarationAtom;
                Object argument = classDeclarationAtom.getArgument();
                if (argument instanceof Variable) {
                    Variable variable = (Variable) argument;
                    if (variable.getName().equals("patient")) {
                        patientDeclarationAtom = declarationAtom;
                        patientVariable = variable;
                    }
                }
            }
            if (declarationAtom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) declarationAtom;
                String predicate = declarationAtom.predicate;
                switch (predicate) {
                    case AGE_PROPERTY:
                        ageVariable = (Variable) twoArgumentsAtom.getArgument2();
                        ageAtom = twoArgumentsAtom;
                        ageRange = alignRange(ageVariable, rule.getBodyAtoms(), ageRange);
                        break;
                }
            }
        }
        for (AbstractAtom bodyAtom : rule.getBodyAtoms()) {
            if (bodyAtom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) bodyAtom;
                String predicate = bodyAtom.predicate;
                switch (predicate) {
                    case AGE_PROPERTY:
                        ageVariable = (Variable) twoArgumentsAtom.getArgument2();
                        ageAtom = twoArgumentsAtom;
                        break;
                    case HAS_SYMPTOM_PROPERTY:
                        symptoms.add((Entity) twoArgumentsAtom.getArgument2());
                        break;
                    case NEGATIVE_TEST_PROPERTY:
                        negativeTests.add((Entity) twoArgumentsAtom.getArgument2());
                        break;
                }
            }
        }
        for (AbstractAtom headAtom : rule.getHeadAtoms()) {
            if (headAtom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) headAtom;
                String predicate = headAtom.predicate;
                switch (predicate) {
                    case HAS_DISEASE_PROPERTY:
                        diseases.add((Entity) twoArgumentsAtom.getArgument2());
                        break;
                }
            }
        }
    }

    public RuleBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RuleBuilder withSymptom(Entity symptom) {
        this.symptoms = singleton(symptom);
        return this;
    }

    public RuleBuilder withSymptoms(Collection<Entity> symptoms) {
        this.symptoms = new HashSet<>(symptoms);
        return this;
    }

    public Collection<Entity> getSymptoms() {
        return symptoms;
    }

    public RuleBuilder withNegativeTest(Entity negativeTest) {
        this.negativeTests = singleton(negativeTest);
        return this;
    }

    public RuleBuilder withNegativeTests(Collection<Entity> negativeTests) {
        this.negativeTests = new HashSet<>(negativeTests);
        return this;
    }

    public Collection<Entity> getNegativeTests() {
        return negativeTests;
    }

    public RuleBuilder withDisease(Entity disease) {
        this.diseases = singleton(disease);
        return this;
    }

    public RuleBuilder withDiseases(Collection<Entity> diseases) {
        this.diseases = new HashSet<>(diseases);
        return this;
    }

    public Collection<Entity> getDiseases() {
        return diseases;
    }

    public RuleBuilder withAge(Range<Integer> ageRange) {
        this.ageRange = ageRange;
        return this;
    }

    public Rule build() {
        Set<AbstractAtom> declarationAtoms = new HashSet<>();
        Set<AbstractAtom> headAtoms = new HashSet<>();
        Set<AbstractAtom> bodyAtoms = new HashSet<>();

        if (patientDeclarationAtom != null)
            declarationAtoms.add(patientDeclarationAtom);
        bodyAtoms.addAll(symptoms.stream()
                .map(symptom -> new TwoArgumentsAtom<>(HAS_SYMPTOM_PROPERTY, patientVariable, symptom))
                .collect(toSet()));
        bodyAtoms.addAll(negativeTests.stream()
                .map(negativeTest -> new TwoArgumentsAtom<>(NEGATIVE_TEST_PROPERTY, patientVariable, negativeTest))
                .collect(toSet()));
        headAtoms.addAll(diseases.stream()
                .map(disease -> new TwoArgumentsAtom<>(HAS_DISEASE_PROPERTY, patientVariable, disease))
                .collect(toSet()));
        if (ageRange != null) {
            declarationAtoms.add(ageAtom);
            bodyAtoms.addAll(getAgeAtoms());
        }

        return new Rule(name, declarationAtoms, bodyAtoms, headAtoms);
    }

    private Collection<AbstractAtom> getAgeAtoms() {
        Collection<AbstractAtom> ageAtoms = new ArrayList<>();
        if (ageRange.hasLowerBound()) {
            switch (ageRange.lowerBoundType()) {
                case OPEN:
                    ageAtoms.add(new TwoArgumentsAtom<>(GREATER_THAN_PROPERTY, SWRLB_PREFIX, ageVariable, ageRange.lowerEndpoint()));
                    break;
                case CLOSED:
                    ageAtoms.add(new TwoArgumentsAtom<>(GREATER_THAN_OR_EQUAL_PROPERTY, SWRLB_PREFIX, ageVariable, ageRange.lowerEndpoint()));
                    break;
            }
        }
        if (ageRange.hasUpperBound()) {
            switch (ageRange.upperBoundType()) {
                case OPEN:
                    ageAtoms.add(new TwoArgumentsAtom<>(LESS_THAN_PROPERTY, SWRLB_PREFIX, ageVariable, ageRange.upperEndpoint()));
                    break;
                case CLOSED:
                    ageAtoms.add(new TwoArgumentsAtom<>(LESS_THAN_OR_EQUAL_PROPERTY, SWRLB_PREFIX, ageVariable, ageRange.upperEndpoint()));
                    break;
            }
        }

        return ageAtoms;
    }

    private Range<Integer> alignRange(Variable variable, Collection<AbstractAtom> atoms, Range<Integer> range) {
        for (AbstractAtom atom : atoms) {
            if (atom instanceof TwoArgumentsAtom) {
                TwoArgumentsAtom twoArgumentsAtom = (TwoArgumentsAtom) atom;
                if (twoArgumentsAtom.getArgument1().equals(variable)) {
                    String predicate = twoArgumentsAtom.getPredicate();
                    int value = (int) twoArgumentsAtom.getArgument2();
                    switch (predicate) {
                        case LESS_THAN_PROPERTY:
                            range = range.intersection(lessThan(value));
                            break;
                        case LESS_THAN_OR_EQUAL_PROPERTY:
                            range = range.intersection(atMost(value));
                            break;
                        case GREATER_THAN_PROPERTY:
                            range = range.intersection(greaterThan(value));
                            break;
                        case GREATER_THAN_OR_EQUAL_PROPERTY:
                            range = range.intersection(atLeast(value));
                            break;
                        case EQUAL_PROPERTY:
                            range = range.intersection(Range.singleton(value));
                            break;
                    }
                }
            }
        }
        return range;
    }
}
