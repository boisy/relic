import org.antlr.runtime.*;

public class rbfe
{
	static int errorCount = 0, warningCount = 0;
	
    public static void main(String[] args) throws Exception
	{
        ANTLRInputStream input = new ANTLRInputStream(System.in);
        RaginBasicLexer lexer = new RaginBasicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RaginBasicParser parser = new RaginBasicParser(tokens);
        parser.program();
    }

	public static void reportError(String errorString)
	{
//		errorCount++;
		System.err.println("ERROR: " + errorString);
	}
	
	public static void reportWarning(String errorString)
	{
		warningCount++;
		System.err.println("WARNING: " + errorString);
	}
}
