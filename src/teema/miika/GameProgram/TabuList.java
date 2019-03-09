package teema.miika.GameProgram;

import java.util.HashMap;
import java.util.Map;

public class TabuList {
    final int MAX_SIZE = 3;
    int _lastIndex = 0;

    private Map<Integer, Match> _tabList = new HashMap<>();

    /// <summary>
    ///     Add match to tabulist
    /// </summary>
    public void add(Match match) {
        if (!_tabList.containsValue(match)) {
            if (_tabList.size() >= MAX_SIZE)
                _tabList.remove(_lastIndex - MAX_SIZE);
            _tabList.put(_lastIndex, match);
            _lastIndex++;
        }
    }

    /// <summary>
    ///     Check if tabulist contains match
    /// </summary>
    public boolean contains(Match match) {
        return _tabList.containsValue(match);
    }
}
