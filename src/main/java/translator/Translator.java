package translator;

import translator.Storage.Immediate;
import translator.Storage.Memory;
import translator.Storage.Register;
import translator.TranslatorAction.DecreaseOffset;
import translator.TranslatorAction.EmptyLine;
import translator.TranslatorAction.GenerateInstruction;
import translator.TranslatorAction.IncreaseOffset;
import tree.SemanticAnalyzer;
import tree.Tree;
import triad.Reference;
import triad.Reference.ConstantReference;
import triad.Reference.FunctionReference;
import triad.Reference.TriadReference;
import triad.Reference.VariableReference;
import triad.Triad;
import triad.Triad.Action;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Translator {

    private Triad[] triads;
    private SemanticAnalyzer semantic;

    private int[] usages;

    private int[] registers = new int[Storage.REGISTERS.length];
    private Storage[] storages;

    private ArrayList<TranslatorAction> actions = new ArrayList<>();

    private int localStorage, prologIndex;
    private int[] temporary;

    public Translator(Triad[] t, SemanticAnalyzer analyzer) {
        triads = t;
        semantic = analyzer;

        Arrays.fill(registers, -1);
        storages = new Storage[triads.length];

        usages = new int[triads.length];
        for (Triad triad : triads) {
            if (triad.ref1 instanceof TriadReference) {
                usages[((TriadReference) triad.ref1).index]++;
            }
            if (triad.ref2 instanceof TriadReference) {
                usages[((TriadReference) triad.ref2).index]++;
            }
        }
    }

    public void translate(boolean silent) throws Exception {
        ArrayList<Tree> global = new ArrayList<>();
        Tree main = null;

        Tree current = semantic.getRoot().left.right;
        while (current != null) {
            if (current.global) {
                global.add(current);
            }
            if (current.lexeme.getName().equals("main")) {
                main = current;
            }

            current = current.left;
        }

        addInstruction(".386");
        addInstruction(".MODEL flat, stdcall");
        emptyLine();

        if (global.size() != 0) {
            addInstruction(".DATA");
            increaseOffset();
            for (Tree tree : global) {
                addInstruction(String.format("%-12s", tree.lexeme.getName()), "DD");
            }
            decreaseOffset();
            emptyLine();
        }


        addInstruction(String.format("%-12s", "_break"), "DD 13");
        decreaseOffset();
        emptyLine();

        addInstruction(".CODE");
        increaseOffset();
        for (int i = 0; i < triads.length; i++) {
            Triad triad = triads[i];
            switch (triad.action) {
                case proc:
                    addInstruction("proc", ((FunctionReference) triad.ref1).name);
                    increaseOffset();
                    break;
                case call: {
                    String arg = ((FunctionReference) triad.ref1).name;
                    switch (arg) {
                        case "#prolog":
                            addInstruction("push", "ebp");
                            addInstruction("mov", "ebp,", "esp");
                            prologIndex = addInstruction("sub", "esp,", ((ConstantReference) triad.ref2).value);
                            emptyLine();

                            localStorage = (int) ((ConstantReference) triad.ref2).value;
                            temporary = new int[0];

                            break;
                        case "#epilog":
                            addInstruction("label_" + i + ":", "mov", "esp,", "ebp");
                            addInstruction("pop", "ebp");
                            addInstruction("ret", ((ConstantReference) triad.ref2).value);

                            String size = localStorage + " + " + (temporary.length * 4);
                            actions.set(prologIndex, new GenerateInstruction("sub", "esp,", "[" + size + "]"));

                            break;
                        default:
                            Register eax = getOrFreeRegister(0); // free eax for return value
                            addInstruction("call", arg);
                            updateStorage(i, eax);
                            emptyLine();
                    }
                    break;
                }
                case endp:
                    decreaseOffset();
                    addInstruction("endp");
                    if (i != triads.length - 1) {
                        emptyLine();
                    }
                    break;
                case add:
                case sub:
                case mul:
                case div:
                case assign:
                    buildBinaryOperation(i, triad);
                    break;
                case push:
                    addInstruction("push", getOperand(triad.ref1));
                    if (triad.ref1 instanceof TriadReference) {
                        decreaseUsage((TriadReference) triad.ref1);
                    }
                    break;
                case nop: {
                    String label = "label_" + i;
                    addInstruction(label + ":", "nop");
                    increaseOffset();
                    break;
                }
                case jmp:
                case jg: {
                    String to = "label_" + ((TriadReference) triad.ref1).index;
                    if (triad.action == Action.jg) {
                        decreaseOffset();
                    }
                    addInstruction(triad.action, to);
                    emptyLine();
                    break;
                }
                case cmp: {
                    Storage operand = getOperand(triad.ref1);
                    if (operand instanceof Immediate) {
                        Register reg = getOrFreeRegister();
                        addInstruction("mov", reg + ",", operand);
                        operand = reg;
                    }
                    addInstruction("cmp", operand + ",", getOperand(triad.ref2));

                    if (triad.ref1 instanceof TriadReference) {
                        decreaseUsage((TriadReference) triad.ref1);
                    }
                }
                case ret: {
                    Storage operand = getOperand(triad.ref1);
                    addInstruction("mov", "eax,", operand);

                    if (triad.ref1 instanceof TriadReference) {
                        decreaseUsage((TriadReference) triad.ref1);
                    }
                }
            }
        }

        decreaseOffset();
        addInstruction("END", main != null ? "main" : "");

        PrintWriter out = new PrintWriter(new FileWriter("main.asm"));
        int offset = 0;

        for (TranslatorAction action : actions) {
            if (action instanceof IncreaseOffset) {
                offset++;
            }
            if (action instanceof DecreaseOffset) {
                offset--;
            }

            if (action instanceof GenerateInstruction) {
                String instruction = ((GenerateInstruction) action).toString(offset);
                out.println(instruction);
                if (!silent) {
                    System.out.println(instruction);
                }
            }
            if (action instanceof EmptyLine) {
                out.println();
                if (!silent) {
                    System.out.println();
                }
            }
        }
        out.close();
    }

    private int addInstruction(Object... args) {
        actions.add(new GenerateInstruction(args));
        return actions.size() - 1;
    }

    private void emptyLine() {
        actions.add(new EmptyLine());
    }

    private void increaseOffset() {
        actions.add(new IncreaseOffset());
    }

    private void decreaseOffset() {
        actions.add(new DecreaseOffset());
    }

    private void buildBinaryOperation(int index, Triad triad) {
        Storage left = getOperand(triad.ref1), right = getOperand(triad.ref2);

        boolean replaceLeft = triad.ref1 instanceof TriadReference && usages[((TriadReference) triad.ref1).index] == 1;
        boolean replaceRight = triad.ref2 instanceof TriadReference && usages[((TriadReference) triad.ref2).index] == 1;

        if (triad.action == Action.assign) {
            replaceLeft = true;
        }

        Storage target = null;
        switch (triad.action) {
            case add:
            case sub:
            case assign: {
                String action = null;
                switch (triad.action) {
                    case add:
                        action = "add";
                        break;
                    case sub:
                        action = "sub";
                        break;
                    case assign:
                        action = "mov";
                }

                if (left instanceof Register && replaceLeft) {
                    addInstruction(action, left + ",", right);
                    target = left;
                } else if (triad.action == Action.add && right instanceof Register && replaceRight) {
                    addInstruction(action, right + ",", left);
                    target = right;
                } else if (left instanceof Memory && replaceLeft && (right instanceof Immediate || right instanceof Register)) {
                    addInstruction(action, left + ",", right);
                    target = left;
                } else {
                    target = getOrFreeRegister();
                    if (triad.action != Action.assign) {
                        addInstruction("mov", target + ",", getOperand(triad.ref1));
                        addInstruction(action, target + ",", getOperand(triad.ref2));
                    } else {
                        addInstruction("mov", target + ",", getOperand(triad.ref2));
                        addInstruction(action, getOperand(triad.ref1) + ",", target);
                    }
                }
                break;
            }
            case mul: {
                getOrFreeRegister(3); // free edx
                left = getOperand(triad.ref1); right = getOperand(triad.ref2);

                if (left instanceof Register && ((Register) left).index == 0 && replaceLeft) {
                    // if first operand in eax and can be replaced
                    addInstruction("mul", convertImmediate(right));
                    target = left;
                } else if (right instanceof Register && ((Register) right).index == 0 && replaceRight) {
                    // if second operand in eax and can be replaced
                    addInstruction("mul", convertImmediate(left));
                    target = right;
                } else {
                    target = getOrFreeRegister(0);
                    addInstruction("mov", target + ",", getOperand(triad.ref1));
                    addInstruction("mul", convertImmediate(getOperand(triad.ref2)));
                }

                break;
            }
            case div: {
                Register edx = getOrFreeRegister(3); // free edx
                left = getOperand(triad.ref1); right = getOperand(triad.ref2);

                addInstruction("mov", edx + ",", 0);
                if (left instanceof Register && ((Register) left).index == 0 && replaceLeft) {
                    addInstruction("div", convertImmediate(right));
                    target = left;
                } else {
                    target = getOrFreeRegister(0);
                    addInstruction("mov", target + ",", getOperand(triad.ref1));
                    addInstruction("div", convertImmediate(getOperand(triad.ref2)));
                }
            }
        }

        if (triad.action == Action.assign && index != triads.length - 1) {
            emptyLine();
        }

        if (triad.ref1 instanceof TriadReference) {
            decreaseUsage((TriadReference) triad.ref1);
        }
        if (triad.ref2 instanceof TriadReference) {
            decreaseUsage((TriadReference) triad.ref2);
        }

        if (target != null) {
            updateStorage(index, target);
        }

        //System.out.println(Arrays.toString(registers));
    }

    private Storage convertImmediate(Storage storage) {
        if (!(storage instanceof Immediate)) {
            return storage;
        }
        Register ebx = getOrFreeRegister(1);
        addInstruction("mov", ebx + ",", storage);
        return ebx;
    }

    private Storage getOperand(Reference reference) {
        if (reference instanceof ConstantReference) {
            return new Immediate(((ConstantReference) reference).value);
        }
        if (reference instanceof VariableReference) {
            return new Memory(((VariableReference) reference).variable);
        }

        int index = ((TriadReference) reference).index;
        return storages[index];
    }

    private void updateStorage(int index, Storage storage) {
        storages[index] = storage;
        if (storage instanceof Register) {
            registers[((Register) storage).index] = index;
        }
    }

    private void decreaseUsage(TriadReference ref) {
        usages[ref.index]--;
        if (usages[ref.index] == 0) {
            if (storages[ref.index] instanceof Register) {
                registers[((Register) storages[ref.index]).index] = -1;
            } else if (storages[ref.index] instanceof Memory) {
                Memory mem = (Memory) storages[ref.index];
                if (mem.index != -1) {
                    temporary[mem.index] = -1;
                }
            }
        }
    }

    private Register getOrFreeRegister() {
        for (int i = 0; i < registers.length; i++) {
            if (registers[i] == -1) {
                return new Register(i);
            }
        }
        return getOrFreeRegister(0);
    }

    private Register getOrFreeRegister(int i) {
        Register reg = new Register(i);
        if (registers[i] == -1) {
            return reg;
        }

        Storage storage = null;
        for (int j = 0; j < registers.length && storage == null; j++) {
            if (j != i && registers[j] == -1) {
                storage = new Register(j);
            }
        }

        if (storage == null) {
            storage = getFromTemporaryArea();
        }

        addInstruction("mov", storage + ",", reg);

        storages[registers[i]] = storage;
        registers[i] = -1;

        return reg;
    }

    private Memory getFromTemporaryArea() {
        for (int i = 0; i < temporary.length; i++) {
            if (temporary[i] == -1) {
                return new Memory(-(localStorage + (i + 1) * 4), i);
            }
        }

        int[] tmp = new int[temporary.length + 1];
        System.arraycopy(temporary, 0, tmp, 0, temporary.length);
        tmp[temporary.length] = -1;

        temporary = tmp;
        return new Memory(-(localStorage + tmp.length * 4), tmp.length - 1);
    }

}
