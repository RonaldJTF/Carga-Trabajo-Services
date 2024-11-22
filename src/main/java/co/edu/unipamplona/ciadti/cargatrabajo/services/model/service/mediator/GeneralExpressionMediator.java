package co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.mediator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.edu.unipamplona.ciadti.cargatrabajo.services.exception.CiadtiException;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ReglaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.ReglaService;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.service.VariableService;
import lombok.RequiredArgsConstructor;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeneralExpressionMediator {
    private final VariableService variableService;
    private final ReglaService reglaService;

    /**
     * Completa cada variable con la expresión de los nombres de las variables que relaciona.
     * @param inputVariables: Lista de variables a completar la expresión de su valor.
     * @param relatedVariables: Lista de variables relacionadas con la lista de variables de entrada.
     * @return
     * @throws CiadtiException
     */
    public List<VariableEntity> completeVariableInformation(List<VariableEntity> inputVariables, List<VariableEntity> relatedVariables) throws CiadtiException{
        for (VariableEntity e : inputVariables){
            String expresion = this.getExpressionWithVariableNames(e.getValor(), relatedVariables);
            e.setExpresionValor(expresion);
        }
        return inputVariables;
    }

     /**
     * Completa cada regla con la expresión de los nombres de las variables que relaciona en suis condiciones.
     * @param inputRules: Lista de reglas a completar la expresión de sus condiciones.
     * @param relatedVariables: Lista de variables relacionadas con la lista de reglas de entrada.
     * @return
     * @throws CiadtiException
     */
    public List<ReglaEntity> completeRuleInformation(List<ReglaEntity> inputRules, List<VariableEntity> relatedVariables) throws CiadtiException{
        for (ReglaEntity e : inputRules){
            String expresion = this.getExpressionWithVariableNames(e.getCondiciones(), relatedVariables);
            e.setExpresionCondiciones(expresion);
        }
        return inputRules;
    }

    /**
     * Evalúa las condiciones de una regla en una vigencia.
     * @param reglaId
     * @param validityId
     * @param variables: contiene lista de variables
     * @param primaryVariables: contiene el valor de las variables primarias, por ejemplo, la asignación básica del cargo.
     * @return
     * @throws CiadtiException
     */
    public boolean evaluateRuleConditions(Long reglaId, Long validityId, List<VariableEntity> variables, Map<String, Double> primaryVariables) throws CiadtiException {
        ReglaEntity regla = reglaService.findById(reglaId);
        String condiciones = regla.getCondiciones();
        if (condiciones == null || condiciones.isEmpty()) {
            throw new IllegalArgumentException("No existen condiciones para la regla con ID: " + reglaId);
        }
        return evaluateExpressions(condiciones, validityId, variables, primaryVariables);
    }

    /**
     * Obtiene el valor de una variable en una vigencia, si esta es gestionada por vigencia.
     * @param variableId
     * @param validityId
     * @param variables: contiene lista de variables
     * @param primaryVariables: contiene el valor de las variables primarias, por ejemplo, la asignación básica del cargo.
     * @return
     * @throws CiadtiException
     */
    public double getValueOfVariable(Long variableId, Long validityId, List<VariableEntity> variables, Map<String, Double> primaryVariables) throws CiadtiException {
        String formula = findValueOfVariable(variableId, validityId, variables, primaryVariables);
        return evaluateArithmeticExpression(formula, validityId, variables, primaryVariables); 
    }
    
    /**
     * Obtiene una expresion con relaciones de ids en ella, con sus nombres. 
     * Ej: pasa de $[1]*0.5 + $[5]/2 a SMMLV*0.5 + Bonificación/2
     * @param expression
     * @param variables: contiene lista de variables
     * @return
     * @throws CiadtiException
     */
    public String getExpressionWithVariableNames(String expression, List<VariableEntity> variables) throws CiadtiException {
        Pattern pattern = Pattern.compile("\\$\\[(\\d+)]");
        Matcher matcher = pattern.matcher(expression);
        StringBuffer expressionWithNames = new StringBuffer();
        while (matcher.find()) {
            Long variableId = Long.parseLong(matcher.group(1));
            VariableEntity variable = this.findVariable(variableId, variables);
            matcher.appendReplacement(expressionWithNames,  variable.getNombre());
        }
        matcher.appendTail(expressionWithNames);
        return expressionWithNames.toString();
    }

    /**
     * Evalúa una expresion aritmetica para variables que pueden depender de una vigencia.
     * @param expression
     * @param idValidity
     * @param variables: contiene lista de variables
     * @param primaryVariables: contiene el valor de las variables primarias, por ejemplo, la asignación básica del cargo.
     * @return
     * @throws CiadtiException
     */
    private double evaluateArithmeticExpression(String expression, Long idValidity, List<VariableEntity> variables, Map<String, Double> primaryVariables) throws CiadtiException {
        Pattern pattern = Pattern.compile("\\$\\[(\\d+)]");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            Long referencedId = Long.parseLong(matcher.group(1));
            double referencedValue;
            referencedValue = this.getValueOfVariable(referencedId, idValidity, variables, primaryVariables);
            expression = expression.replace(matcher.group(), String.valueOf(referencedValue));
        }
        Expression expressionBuilder = new ExpressionBuilder(expression).build();
        return expressionBuilder.evaluate();
    }

    
    /**
     * Encuentra el valor de una variable. Si la variable es gestionada por vigencia, entonces retorna el valor en esa vigencia,
     * en caso contrario, devuelve la expresión que define su valor.
     * @param variableId
     * @param validityId
     * @param variables: contiene lista de variables
     * @param primaryVariables: contiene el valor de las variables primarias, por ejemplo, la asignación básica del cargo.
     * @return
     * @throws CiadtiException
     */
    private String findValueOfVariable(Long variableId, Long validityId, List<VariableEntity> variables, Map<String, Double> primaryVariables) throws CiadtiException{
        VariableEntity variable = this.findVariable(variableId, variables);
        if (variable.getValor() != null && !variable.getValor().isEmpty()) {
            if(primaryVariables.get(variable.getValor()) != null){
                return primaryVariables.get(variable.getValor()).toString();
            }
            return variable.getValor();
        }else{
            return String.valueOf(variableService.findValueInValidity(variableId, validityId));
        }
    }

    /**
     * Encuentra una variable en la lista de variables;
     * @param variables
     * @return
     */
    private VariableEntity findVariable(Long variableId, List<VariableEntity> variables){
        return variables.stream()
        .filter(e -> e.getId() == variableId)
        .findFirst()
        .orElse(null);
    }

    
    /**
     * Evalua multiples condiciones booleanas cuyos valores de variables pueden depender de una vigencia.
     * @param conditions: condiciones
     * @param idValidity: Id de la vigencia
     * @param variables: contiene lista de variables
     * @param primaryVariables: contiene el valor de las variables primarias, por ejemplo, la asignación básica del cargo.
     * @return
     * @throws CiadtiException
     */
    private boolean evaluateExpressions(String conditions, Long idValidity, List<VariableEntity> variables, Map<String, Double> primaryVariables) throws CiadtiException {
        String[] expressions = conditions.split("&");
        for (String expression : expressions) {
            if (!evaluateBooleanExpression(expression.trim(), idValidity, variables, primaryVariables)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Evalua una expresión booleana en una vigencia
     * @param expression
     * @param validityId
     * @param variables: contiene lista de variables
     * @param primaryVariables: contiene el valor de las variables primarias, por ejemplo, la asignación básica del cargo.
     * @return
     * @throws CiadtiException
     */
    private boolean evaluateBooleanExpression(String expression, Long validityId, List<VariableEntity> variables, Map<String, Double> primaryVariables) throws CiadtiException {
        String[] partes = expression.split("(?<=\\b|\\W)(?=<=|>=|!=|==|<|>)|(?<=<=|>=|!=|==|<|>)(?=\\b|\\W)");
        double ladoIzquierdo, ladoDerecho;
    
        if (partes.length == 1) { 
            ladoIzquierdo = evaluateArithmeticExpression(partes[0].trim(), validityId, variables, primaryVariables);
            return ladoIzquierdo != 0; 
        } else if (partes.length == 3) {
            ladoIzquierdo = evaluateArithmeticExpression(partes[0].trim(), validityId, variables, primaryVariables);
            ladoDerecho = evaluateArithmeticExpression(partes[2].trim(), validityId, variables, primaryVariables);
            switch (partes[1].trim()) {
                case ">":
                    return ladoIzquierdo > ladoDerecho;
                case "<":
                    return ladoIzquierdo < ladoDerecho;
                case ">=":
                    return ladoIzquierdo >= ladoDerecho;
                case "<=":
                    return ladoIzquierdo <= ladoDerecho;
                case "==":
                    return ladoIzquierdo == ladoDerecho;
                case "!=":
                    return ladoIzquierdo != ladoDerecho;
                default:
                    throw new IllegalArgumentException("Operador desconocido: " + partes[1].trim());
            }
        } else {
            throw new IllegalArgumentException("La expresión no tiene un formato válido: " + expression);
        }
    }
}