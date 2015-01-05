package org.sonar.samples.java;

import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.List;

@Rule(key = ForbiddenAnnotationCheck.KEY,
  name = "Avoid usage of annotation",
  description = "This rule detects usage of configured annotation",
  tags = {"example"})
public class ForbiddenAnnotationCheck extends BaseTreeVisitor implements JavaFileScanner {

  public static final String KEY = "ForbiddenAnnotations";
  private static final String DEFAULT_VALUE = "Inject";
  private final RuleKey RULE_KEY = RuleKey.of(MyJavaRulesDefinition.REPOSITORY_KEY, KEY);

  private JavaFileScannerContext context;

  /**
   * Name of the annotation to avoid. Value can be set by users in Quality profiles.
   * The key
   */
  @RuleProperty(
    defaultValue = DEFAULT_VALUE,
    description = "Name of the annotation to avoid, without the prefix @, for instance 'Override'")
  String name;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    this.context = context;

    scan(context.getTree());

    System.out.println(PrinterVisitor.print(context.getTree()));
  }

  @Override
  public void visitMethod(MethodTree tree) {
    List<AnnotationTree> annotations = tree.modifiers().annotations();
    for (AnnotationTree annotationTree : annotations) {
      if (annotationTree.annotationType().is(Tree.Kind.IDENTIFIER)) {
        IdentifierTree idf = (IdentifierTree) annotationTree.annotationType();
        System.out.println(idf.name());

        if (idf.name().equals(name)) {
          context.addIssue(idf, RULE_KEY, String.format("Avoid using annotation @%s", name));
        }
      }
    }

    // The call to the super implementation allows to continue the visit of the AST.
    // Be careful to always call this method to visit every node of the tree.
    super.visitMethod(tree);
  }
}
