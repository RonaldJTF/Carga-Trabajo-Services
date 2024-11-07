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
     * Obtiene las variables con la expresión de los nombres de las variables que relaciona.
     * @param filter
     * @return
     * @throws CiadtiException
     */
    public List<VariableEntity> getVariables(VariableEntity filter) throws CiadtiException{
        List<VariableEntity> result = variableService.findAllFilteredBy(filter);
        List<VariableEntity> variables = variableService.findAll();
        for (VariableEntity e : result){
            String expresion = this.getExpressionWithVariableNames(e.getValor(), variables);
            e.setExpresionValor(expresion);
        }
        return result;
    }

    /**
     * Obtiene la lista de reglas con su expresión formateada con los nombres de las variables
     * @param filter
     * @return
     * @throws CiadtiException
    */
    public List<ReglaEntity> getRules(ReglaEntity filter) throws CiadtiException{
        List<ReglaEntity> result = reglaService.findAllFilteredBy(filter);
        List<VariableEntity> variables = variableService.findAll();
        for (ReglaEntity e : result){
            String expresion = this.getExpressionWithVariableNames(e.getCondiciones(), variables);
            e.setExpresionCondiciones(expresion);
        }
        return result;
    }

    /**
     * Evalúa las condiciones de una regla en una vigencia.
     * @param reglaId
     * @param idVigencia
     * @param allVariablesInDB: contiene todas las variables en BD
     * @return
     * @throws CiadtiException
     */
    public boolean evaluateRuleConditions(Long reglaId, Long idVigencia, List<VariableEntity> allVariablesInDB) throws CiadtiException {
        ReglaEntity regla = reglaService.findById(reglaId);
        String condiciones = regla.getCondiciones();
        if (condiciones == null || condiciones.isEmpty()) {
            throw new IllegalArgumentException("No existen condiciones para la regla con ID: " + reglaId);
        }
        return evaluateExpressions(condiciones, idVigencia, allVariablesInDB);
    }

    /**
     * Obtiene el valor de una variable en una vigencia, si esta es gestionada por vigencia.
     * @param idVariable
     * @param idVigencia
     * @param allVariablesInDB: Contiene todas las variables en BD
     * @return
     * @throws CiadtiException
     */
    public double getValueOfVariable(Long idVariable, Long idVigencia, List<VariableEntity> allVariablesInDB) throws CiadtiException {
        String formula = findValueOfVariable(idVariable, idVigencia, allVariablesInDB);
        return evaluateArithmeticExpression(formula, idVigencia, allVariablesInDB); 
    }
    
    /**
     * Evalúa una expresion aritmetica para variables que pueden depender de una vigencia.
     * @param expression
     * @param idValidity
     * @param allVariablesInDB: contiene todas las variables en BD
     * @return
     * @throws CiadtiException
     */
    private double evaluateArithmeticExpression(String expression, Long idValidity, List<VariableEntity> allVariablesInDB) throws CiadtiException {
        Pattern pattern = Pattern.compile("\\$\\[(\\d+)]");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            Long referencedId = Long.parseLong(matcher.group(1));
            double referencedValue;
            referencedValue = this.getValueOfVariable(referencedId, idValidity, allVariablesInDB);
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
     * @param allVariablesInDB: contiene todas las variables en BD
     * @return
     * @throws CiadtiException
     */
    private String findValueOfVariable(Long variableId, Long validityId, List<VariableEntity> allVariablesInDB) throws CiadtiException{
        VariableEntity variable = this.findVariable(variableId, allVariablesInDB);
        if (variable.getValor() != null && !variable.getValor().isEmpty()) {
            return variable.getValor();
        }else{
            return String.valueOf(variableService.findValueInValidity(variableId, validityId));
        }
    }

    /**
     * Encuentra una variable en la lista de variables;
     * @param allVariablesInDB
     * @return
     */
    private VariableEntity findVariable(Long variableId, List<VariableEntity> allVariablesInDB){
        return allVariablesInDB.stream()
        .filter(e -> e.getId() == variableId)
        .findFirst()
        .orElse(null);
    }

    /**
     * Obtiene una expresion con relaciones de ids en ella, con sus nombres. 
     * Ej: pasa de $[1]*0.5 + $[5]/2 a SMMLV*0.5 + Bonificación/2
     * @param expression
     * @param allVariablesInDB: contiene todas las variables en BD
     * @return
     * @throws CiadtiException
     */
    private String getExpressionWithVariableNames(String expression, List<VariableEntity> allVariablesInDB) throws CiadtiException {
        Pattern pattern = Pattern.compile("\\$\\[(\\d+)]");
        Matcher matcher = pattern.matcher(expression);
        StringBuffer formulaConNombres = new StringBuffer();
        while (matcher.find()) {
            Long idVariable = Long.parseLong(matcher.group(1));
            VariableEntity variable = this.findVariable(idVariable, allVariablesInDB);
            matcher.appendReplacement(formulaConNombres,  variable.getNombre());
        }
        matcher.appendTail(formulaConNombres);
        return formulaConNombres.toString();
    }
    
    /**
     * Evalua multiples condiciones booleanas cuyos valores de variables pueden depender de una vigencia.
     * @param conditions: condiciones
     * @param idValidity: Id de la vigencia
     * @param allVariablesInDB: contiene todas las variables en BD
     * @return
     * @throws CiadtiException
     */
    private boolean evaluateExpressions(String conditions, Long idValidity, List<VariableEntity> allVariablesInDB) throws CiadtiException {
        String[] expressions = conditions.split("&");
        for (String expression : expressions) {
            if (!evaluateBooleanExpression(expression.trim(), idValidity, allVariablesInDB)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Evalua una expresión booleana en una vigencia
     * @param expression
     * @param validityId
     * @param allVariablesInDB: contiene todas las variables en BD
     * @return
     * @throws CiadtiException
     */
    private boolean evaluateBooleanExpression(String expression, Long validityId, List<VariableEntity> allVariablesInDB) throws CiadtiException {
        String[] partes = expression.split("(?<=\\b|\\W)(?=<=|>=|!=|==|<|>)|(?<=<=|>=|!=|==|<|>)(?=\\b|\\W)");
        double ladoIzquierdo, ladoDerecho;
    
        if (partes.length == 1) { 
            ladoIzquierdo = evaluateArithmeticExpression(partes[0].trim(), validityId, allVariablesInDB);
            return ladoIzquierdo != 0; 
        } else if (partes.length == 3) {
            ladoIzquierdo = evaluateArithmeticExpression(partes[0].trim(), validityId, allVariablesInDB);
            ladoDerecho = evaluateArithmeticExpression(partes[2].trim(), validityId, allVariablesInDB);
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