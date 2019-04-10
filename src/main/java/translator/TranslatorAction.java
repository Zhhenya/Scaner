package translator;

public abstract class TranslatorAction {
    
    public static class EmptyLine extends TranslatorAction { }
    
    public static class IncreaseOffset extends TranslatorAction { }
    
    public static class DecreaseOffset extends TranslatorAction { }
    
    public static class GenerateInstruction extends TranslatorAction {
        
        public final Object[] instruction;
        
        public GenerateInstruction(Object... data) {
            instruction = data;
        }
        
        public String toString(int offset) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < offset; i++) builder.append("\t");
            for(Object s : instruction) builder.append(String.format("%s ", s));
            return builder.toString();
        }
        
    }
    
}
