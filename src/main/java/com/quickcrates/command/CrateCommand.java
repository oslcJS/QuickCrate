package com.quickcrates.command;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import com.quickcrates.QuickCrates;
public class CrateCommand implements CommandExecutor {
    private final QuickCratesCommand delegate;
    public CrateCommand(QuickCrates p){ this.delegate = new QuickCratesCommand(p); }
    public boolean onCommand(@NotNull CommandSender s,@NotNull Command c,@NotNull String l,@NotNull String[] a){
        return delegate.onCommand(s,c,l,a);
    }
}
