#!/usr/bin/env python3
import argparse
import json
import statistics
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path


def project_transcript_dir(cwd: Path) -> Path:
    return Path.home() / ".claude" / "projects" / str(cwd.resolve()).replace("/", "-")


def read_entries(path: Path):
    with path.open(encoding="utf-8", errors="replace") as handle:
        for line in handle:
            line = line.strip()
            if not line:
                continue
            try:
                yield json.loads(line)
            except json.JSONDecodeError:
                continue


def content_blocks(entry):
    content = (entry.get("message") or {}).get("content")
    return content if isinstance(content, list) else []


def first_prompt(entries):
    for entry in entries:
        if entry.get("type") != "user":
            continue
        content = (entry.get("message") or {}).get("content")
        if isinstance(content, str):
            return content
        for block in content or []:
            if isinstance(block, dict) and block.get("type") == "text":
                return block.get("text", "")
    return None


class Transcript:
    def __init__(self, entries):
        self.contexts = []
        self.cost = 0
        self.compactions = 0
        self.reads = Counter()
        self.spawns = []
        self.started = None
        seen_messages = set()
        for entry in entries:
            timestamp = entry.get("timestamp")
            if timestamp and self.started is None:
                self.started = timestamp
            if entry.get("subtype") == "compact_boundary" or entry.get("isCompactSummary"):
                self.compactions += 1
            for block in content_blocks(entry):
                if not isinstance(block, dict) or block.get("type") != "tool_use":
                    continue
                tool_input = block.get("input") or {}
                if block.get("name") == "Read" and tool_input.get("file_path"):
                    self.reads[tool_input["file_path"]] += 1
                elif block.get("name") in ("Agent", "Task"):
                    self.spawns.append(
                        (tool_input.get("subagent_type") or "general-purpose", tool_input.get("prompt") or ""),
                    )
            message = entry.get("message") or {}
            usage = message.get("usage")
            if entry.get("type") != "assistant" or not usage:
                continue
            message_id = message.get("id")
            if message_id in seen_messages:
                continue
            seen_messages.add(message_id)
            context = (
                usage.get("input_tokens", 0)
                + usage.get("cache_creation_input_tokens", 0)
                + usage.get("cache_read_input_tokens", 0)
            )
            self.contexts.append(context)
            self.cost += context + usage.get("output_tokens", 0)

    @property
    def turns(self):
        return len(self.contexts)


def percentile(values, fraction):
    ordered = sorted(values)
    return ordered[min(len(ordered) - 1, int(fraction * len(ordered)))]


def kilo(value):
    return f"{value / 1000:.0f}K"


def mega(value):
    return f"{value / 1_000_000:.1f}M"


def agent_type_of(agent_file: Path, entries, prompt_types):
    meta = agent_file.with_suffix(".meta.json")
    if meta.exists():
        try:
            agent_type = json.loads(meta.read_text()).get("agentType")
            if agent_type:
                return agent_type
        except (json.JSONDecodeError, OSError):
            pass
    prompt = first_prompt(entries)
    return prompt_types.get(prompt, "unknown")


def main():
    parser = argparse.ArgumentParser(
        description="Summarise Claude Code token usage for this project's sessions and subagent runs.",
    )
    parser.add_argument("--dir", type=Path, help="transcript directory (default: derived from the current directory)")
    parser.add_argument("--since", help="only sessions started on or after this date (YYYY-MM-DD)")
    parser.add_argument("--min-turns", type=int, default=1, help="skip sessions with fewer main turns")
    parser.add_argument("--top", type=int, default=10, help="how many most-read files to list")
    args = parser.parse_args()

    root = args.dir or project_transcript_dir(Path.cwd())
    if not root.is_dir():
        parser.error(f"no transcript directory at {root}")
    since = datetime.fromisoformat(args.since).replace(tzinfo=timezone.utc) if args.since else None

    runs_by_type = defaultdict(list)
    reads = Counter()
    print(f"{'session':<10} {'started':<17} {'turns':>6} {'mean':>6} {'p90':>6} {'max':>6} {'compact':>7}  subagents")
    for session_file in sorted(root.glob("*.jsonl")):
        session = Transcript(read_entries(session_file))
        if session.turns < args.min_turns or session.started is None:
            continue
        started = datetime.fromisoformat(session.started.replace("Z", "+00:00"))
        if since and started < since:
            continue
        reads.update(session.reads)
        prompt_types = {prompt: agent_type for agent_type, prompt in session.spawns}
        session_runs = Counter()
        for agent_file in sorted((root / session_file.stem / "subagents").glob("agent-*.jsonl")):
            entries = list(read_entries(agent_file))
            run = Transcript(entries)
            if run.turns == 0:
                continue
            agent_type = agent_type_of(agent_file, entries, prompt_types)
            runs_by_type[agent_type].append(run)
            session_runs[agent_type] += 1
            reads.update(run.reads)
        contexts = session.contexts
        subagents = ", ".join(f"{name}×{count}" for name, count in session_runs.most_common()) or "-"
        print(
            f"{session_file.stem[:8]:<10} {started:%Y-%m-%d %H:%M} {session.turns:>6} "
            f"{kilo(statistics.mean(contexts)):>6} {kilo(percentile(contexts, 0.9)):>6} {kilo(max(contexts)):>6} "
            f"{session.compactions:>7}  {subagents}",
        )

    print(f"\n{'subagent type':<36} {'runs':>5} {'cost/run':>9} {'turns/run':>10} {'start ctx':>10}")
    for agent_type, runs in sorted(runs_by_type.items(), key=lambda item: -len(item[1])):
        print(
            f"{agent_type:<36} {len(runs):>5} {mega(statistics.mean(r.cost for r in runs)):>9} "
            f"{statistics.mean(r.turns for r in runs):>10.0f} {kilo(statistics.mean(r.contexts[0] for r in runs)):>10}",
        )

    print(f"\nmost-read files (main + subagents)")
    for path, count in reads.most_common(args.top):
        print(f"{count:>5}  {path}")


if __name__ == "__main__":
    main()
